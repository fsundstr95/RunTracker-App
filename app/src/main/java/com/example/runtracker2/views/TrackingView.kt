package com.example.runtracker2.views

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Button
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import com.example.runtracker2.R
import com.example.runtracker2.ViewModel
import com.example.runtracker2.model.database.RunData
import com.example.runtracker2.other.TrackingUtility
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay


@Composable
fun TrackingView(viewModel: ViewModel, navigateBack: () -> Unit, navigateToStatistics: () -> Unit) {
    val scaffoldState = rememberScaffoldState()
    val uiSettings = remember { MapUiSettings(zoomControlsEnabled = false) }
    val pathPoints by viewModel.pathPoints2.collectAsState(emptyList())
    val cameraPositionBounds by viewModel.cameraPositionBounds.collectAsState()
    val cameraPositionState = rememberCameraPositionState()
    val context = LocalContext.current

    val runData by viewModel.lastStoredRun.collectAsState(null)
    val lastCameraPositionBounds by viewModel.lastCameraPositionBounds.collectAsState()
    val lastPathPoints by viewModel.lastPathPoints.collectAsState()

    var mapSnapshot by remember { mutableStateOf<Bitmap?>(null) }


    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        scaffoldState = scaffoldState,
        bottomBar = {
            BottomAppBar(
                backgroundColor = MaterialTheme.colors.primary,
                modifier = Modifier.navigationBarsPadding(),
                content = {
                    IconButton(onClick = navigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = navigateToStatistics) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_graph),
                            contentDescription = "Statistics",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val mapView = remember { MapView(context) }

            // Google Map
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AndroidView(
                    factory = {
                        mapView.apply {
                            onCreate(null)
                            getMapAsync { googleMap ->
                                // Move the camera to the bounds if set
                                if (cameraPositionBounds != null) {
                                    googleMap.moveCamera(
                                        CameraUpdateFactory.newLatLngBounds(
                                            cameraPositionBounds!!,
                                            100
                                        )
                                    )
                                }else if (lastCameraPositionBounds !=null){
                                    googleMap.moveCamera(
                                        CameraUpdateFactory.newLatLngBounds(
                                            lastCameraPositionBounds!!,
                                            100
                                        )
                                    )
                                }

                                // Plot the path points as polylines
                                if (!pathPoints.isNullOrEmpty()) {
                                    pathPoints?.forEach { path ->
                                        if (path.isNotEmpty()) {
                                            googleMap.addPolyline(
                                                PolylineOptions()
                                                    .addAll(path)
                                                    .color(Color.Blue.toArgb())
                                            )
                                        }
                                    }
                                }else if (!lastPathPoints.isNullOrEmpty()){
                                    lastPathPoints?.forEach { path ->
                                        if (path.isNotEmpty()) {
                                            googleMap.addPolyline(
                                                PolylineOptions()
                                                    .addAll(path)
                                                    .color(Color.Blue.toArgb())
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Automatically take a snapshot after zooming to bounds and drawing polylines
                LaunchedEffect(cameraPositionBounds, pathPoints) {
                    delay(1000)
                    if (cameraPositionBounds != null && pathPoints?.isNotEmpty() == true) {
                        mapView.getMapAsync { googleMap ->

                            googleMap.snapshot { bitmap ->
                                if (bitmap != null) {
                                    mapSnapshot = bitmap
                                    viewModel.saveRun(bitmap)
                                }
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .background(Color.Cyan)
                    .padding(8.dp)
            ) {
                Text(
                    text = "Distance: ${runData?.distanceInMeters} km\n" +
                            "Duration: ${TrackingUtility.getFormattedStopWatchTime(runData?.timeInMillis ?: 0L)}\n" +
                            "Average Speed: ${runData?.avgSpeedInKMH} km/h\n" +
                            "Calories Burned: ${runData?.caloriesBurned} kcal\n" +
                            "Date: ${TrackingUtility.formatDateTime(runData?.timestampDate ?: 0L)}",
                    style = MaterialTheme.typography.body2
                )
            }

            // Display the dialog with snapshot
            if (showDialog) {
                Dialog(onDismissRequest = { showDialog = false }) {
                    Box(
                        modifier = Modifier
                            .background(Color.White, shape = RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    ) {
                        mapSnapshot?.let { bitmap ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text("Map Snapshot", style = MaterialTheme.typography.h6)
                                Spacer(modifier = Modifier.height(8.dp))
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Map Snapshot",
                                    modifier = Modifier
                                        .size(200.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(onClick = { showDialog = false }) {
                                    Text("Close")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}