package com.example.runtracker2.views

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.BottomAppBar
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.runtracker2.R
import com.example.runtracker2.ViewModel
import com.example.runtracker2.model.service.PolyLine
import com.example.runtracker2.model.service.PolyLines
import com.example.runtracker2.other.Constants.ACTION_PAUSE_SERVICE
import com.example.runtracker2.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runtracker2.other.Constants.ACTION_STOP_SERVICE
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import timber.log.Timber


@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun RunView(
    viewModel: ViewModel,
    requestPermissions: () -> Unit,
    navigateBack: () -> Unit,
    navigateToStatistics: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToTracking: () -> Unit
) {

    val uiSettings = remember { MapUiSettings(zoomControlsEnabled = false) }

    val _pathPoints by viewModel.pathPoints2.collectAsState(emptyList())

    val runTime by viewModel.runTime.collectAsState("00:00:00:00")

    val isTracking by viewModel.isTracking.collectAsState(false)

    val cameraPosition by viewModel.cameraPosition.collectAsState(LatLng(59.383278, 18.029450))

    val cameraPositionState = rememberCameraPositionState()

    val showDialog = remember { mutableStateOf(false) } // Manage dialog visibility

    val pathPointsState = remember {
        mutableStateOf<List<PolyLine>>(
            mutableListOf(
                mutableListOf(
                    LatLng(
                        59.383278,
                        18.029450
                    )
                )
            )
        )
    }

    // Update Path Points
    if (viewModel.pathPoints2.value?.isNullOrEmpty() == false) {
        LaunchedEffect(viewModel.pathPoints2.value?.get(0)?.size) {
            viewModel.pathPoints2.collect { newPoints ->
                if (!newPoints.isNullOrEmpty()) {
                    Timber.d("RunView, newPoints.size:  ${newPoints[0].size}")
                }
                // Ensure the data is not null
                if (newPoints != null) {
                    pathPointsState.value = newPoints
                } else {
                    pathPointsState.value = emptyList()
                }
            }
        }
    }
    /*LaunchedEffect(_pathPoints) {
        if (_pathPoints != null) {
            pathPointsState.value = _pathPoints as List<PolyLine>
        }
    }*/

    LaunchedEffect(cameraPosition) {
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(cameraPosition as LatLng, 16f) // Zoom level

        //cameraPositionState.position = CameraPosition.fromLatLngZoom(cameraPosition as LatLng, 17f)
        cameraPositionState.animate(cameraUpdate) // Use animate(cameraUpdate) for a smooth transition


    }


    requestPermissions()

    Scaffold(
        topBar = {

            TopAppBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                title = { androidx.compose.material.Text(
                        text = "RunTracker",
                        style = androidx.compose.material.MaterialTheme.typography.h6,
                        fontWeight = FontWeight.Medium
                    )
                        },
                backgroundColor = androidx.compose.material.MaterialTheme.colors.primary,
                contentColor = Color.White

            )
        },

        bottomBar = {
            BottomAppBar(
                backgroundColor = androidx.compose.material.MaterialTheme.colors.primary,
                modifier = Modifier.navigationBarsPadding(),
                contentColor = Color.White,
                elevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {

                    IconButton(onClick = navigateToSettings) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings",tint = Color.White)
                    }

                    IconButton(onClick = navigateToStatistics) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_graph),
                            contentDescription = "Statistics",
                            tint = Color.White
                        )
                    }

                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(5.dp),
                properties = viewModel.state.properties,
                uiSettings = uiSettings,
                cameraPositionState = cameraPositionState
            ) {
                pathPointsState.value.forEach { path ->
                    if (path.isNotEmpty()) {
                        Polyline(
                            points = path,
                            color = Color.Blue
                        )
                    }
                }
            }


            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)

            ) {
                // Runtime Text
                Text(
                    text = runTime ?: "00:00:00",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color.LightGray,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                )


                Spacer(modifier = Modifier.height(16.dp))

                // Row with floating buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.Center
                ) {


                    // Start/Pause button
                    FloatingActionButton(
                        onClick = {
                            if (isTracking == true) {
                                viewModel.sendCommandToService(ACTION_PAUSE_SERVICE)
                            } else {
                                viewModel.sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = if (isTracking == true) "Pause" else "Start",
                            style = MaterialTheme.typography.headlineMedium,


                            )
                    }

                    // Stop button (only visible when running)
                    if (isTracking == true) {
                        FloatingActionButton(
                            onClick = {
                                viewModel.sendCommandToService(ACTION_PAUSE_SERVICE)
                                showDialog.value = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "Stop",
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }

                    }
                }

                // Show Dialog
                if (showDialog.value) {
                    ShowRunOptionsDialog(viewModel = viewModel, dialogVisibilityState = showDialog, navigateToTracking)
                }

            }
        }
    }
}



@Composable
fun ShowRunOptionsDialog(viewModel: ViewModel,
                         dialogVisibilityState: MutableState<Boolean>, navigateToTracking: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { dialogVisibilityState.value = false },
        title = {
            androidx.compose.material.Text(text = "Run Options")
        },
        text = {
            androidx.compose.material.Text(text = "Would you like to save or delete this run?")
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                androidx.compose.material.Button(
                    onClick = {

                        navigateToTracking()
                        dialogVisibilityState.value = false
                    },
                    modifier = Modifier.padding(4.dp)
                ) {
                    androidx.compose.material.Text("Save")
                }

                androidx.compose.material.Button(
                    onClick = {
                        viewModel.deleteRun()
                        dialogVisibilityState.value = false
                    },
                    modifier = Modifier.padding(4.dp)
                ) {
                    androidx.compose.material.Text("Delete")
                }

                androidx.compose.material.Button(
                    onClick = { dialogVisibilityState.value = false },
                    modifier = Modifier.padding(4.dp)
                ) {
                    androidx.compose.material.Text("Cancel")
                }
            }
        }
    )
}

