package com.example.runtracker2.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.runtracker2.R
import com.example.runtracker2.ViewModel
import com.example.runtracker2.model.database.RunData
import com.example.runtracker2.other.Constants.SORTED_BY_AVG_SPEED
import com.example.runtracker2.other.Constants.SORTED_BY_CALORIES_BURNED
import com.example.runtracker2.other.Constants.SORTED_BY_DATE
import com.example.runtracker2.other.Constants.SORTED_BY_DISTANCE
import com.example.runtracker2.other.Constants.SORTED_BY_TIME_IN_MILLIS
import com.example.runtracker2.other.TrackingUtility

@Composable
fun StatisticsView(viewModel: ViewModel, navigateBack: () -> Unit,navigateToStatisticsTotalView: () -> Unit){

    val dataBase by viewModel.storedRunFlow.collectAsState()


    var expanded by remember { mutableStateOf(false) }
    var selectedRun by remember { mutableStateOf<RunData?>(null) }


    Scaffold(

        topBar = {

            TopAppBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                title = { Text("Statistics, Your Runs") },
                actions = {
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.Menu, contentDescription = "Sort Options",tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(onClick = {
                                viewModel.changeSortingKey(SORTED_BY_DATE)
                                expanded = false
                            }) {
                                Text("Sort by Date")
                            }
                            DropdownMenuItem(onClick = {
                                viewModel.changeSortingKey(SORTED_BY_DISTANCE)
                                expanded = false
                            }) {
                                Text("Sort by Distance")
                            }
                            DropdownMenuItem(onClick = {
                                viewModel.changeSortingKey(SORTED_BY_CALORIES_BURNED)
                                expanded = false
                            }) {
                                Text("Sort by Calories")
                            }
                            DropdownMenuItem(onClick = {
                                viewModel.changeSortingKey(SORTED_BY_AVG_SPEED)
                                expanded = false
                            }) {
                                Text("Sort by Average Speed")
                            }
                            DropdownMenuItem(onClick = {
                                viewModel.changeSortingKey(SORTED_BY_TIME_IN_MILLIS)
                                expanded = false
                            }) {
                                Text("Sort by Time")
                            }
                        }
                    }
                }
            )
        },
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

                    TextButton(
                        onClick = navigateToStatisticsTotalView,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_graph),
                            contentDescription = "Statistics",
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Advanced Statistics",
                            style = MaterialTheme.typography.body2,
                            color = Color.White
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
            LazyColumn {
                if (dataBase.isNotEmpty()) {
                    items(dataBase.size) { index ->
                        StatisticsItem(
                            run = dataBase[index]!!,
                            onDelete = { viewModel.deleteRunFromDb(it) },
                            onViewDetails = { selectedRun = it }
                        )
                    }
                }
            }

            selectedRun?.let { run ->
                RunDetailsDialog(run = run, onDismiss = { selectedRun = null })
            }
        }
    }
}

@Composable
fun StatisticsItem(
    run: RunData,
    onDelete: (RunData) -> Unit,
    onViewDetails: (RunData) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Run Image
            run.img?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Run Image",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colors.surface)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Run Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Date: ${TrackingUtility.formatDateTime(run.timestampDate)}",
                    style = MaterialTheme.typography.body2
                )
                Text(
                    text = "Distance: ${run.distanceInMeters} meters",
                    style = MaterialTheme.typography.body2
                )
                Text(
                    text = "Duration: ${TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)}",
                    style = MaterialTheme.typography.body2
                )
                Text(
                    text = "Speed: ${run.avgSpeedInKMH} km/h",
                    style = MaterialTheme.typography.body2
                )
                Text(
                    text = "Calories: ${run.caloriesBurned}",
                    style = MaterialTheme.typography.body2
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Buttons
            Column {
                IconButton(
                    onClick = { onViewDetails(run) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "View Details")
                }

                IconButton(
                    onClick = { onDelete(run) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Run")
                }
            }
        }
    }
}


@Composable
fun RunDetailsDialog(run: RunData, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .background(MaterialTheme.colors.surface, shape = RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Run Details",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text("Date: ${TrackingUtility.formatDateTime(run.timestampDate)}")
                Text("Distance: ${run.distanceInMeters} meters")
                Text("Duration: ${TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)}")
                Text("Speed: ${run.avgSpeedInKMH} km/h")
                Text("Calories: ${run.caloriesBurned}")

                Spacer(modifier = Modifier.height(16.dp))


                run.img?.let {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16 / 9f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colors.surface)
                    ) {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = "Run Image",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close")
                }
            }
        }
    }
}