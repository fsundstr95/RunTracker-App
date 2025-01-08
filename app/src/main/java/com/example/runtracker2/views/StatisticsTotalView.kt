package com.example.runtracker2.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.BottomAppBar
import androidx.compose.material.Button
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.runtracker2.R
import com.example.runtracker2.ViewModel
import com.example.runtracker2.model.database.MonthlySummary
import com.example.runtracker2.model.database.WeeklySummary
import com.example.runtracker2.other.TrackingUtility

@Composable
fun StatisticsTotalView(
    viewModel: ViewModel,
    navigateBack: () -> Unit
) {

    var selectedOption by remember { mutableStateOf("Weekly") }
    val options = listOf("Weekly", "Monthly", "Total")


    val weeklyStats by viewModel.weeklyRunDataFlow.collectAsState(initial = emptyList())
    val monthlyStats by viewModel.monthlyRunDataFlow.collectAsState(initial = emptyList())
    val totalTime by viewModel.totalTimeInMillisFlow.collectAsState(initial = 0L)
    val totalCalories by viewModel.totalCaloriesBurnedFlow.collectAsState(initial = 0)
    val totalDistance by viewModel.totalDistanceInMetersFlow.collectAsState(initial = 0)
    val totalSpeed by viewModel.totalAvgSpeedFlow.collectAsState(initial = 0f)

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
                title = { Text("Advanced Statistics") },
                actions = {
                    // Dropdown Menu
                    Box {
                        var expanded by remember { mutableStateOf(false) }
                        TextButton(onClick = { expanded = true }) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Dropdown",
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = selectedOption,
                                    color = Color.White
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            options.forEach { option ->
                                DropdownMenuItem(onClick = {
                                    selectedOption = option
                                    expanded = false
                                }) {
                                    Text(option)
                                }
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
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (selectedOption) {
                "Weekly" -> weeklyStats?.let {
                    StatisticsContent(
                        title = "Weekly Statistics",
                        stats = it.map { it.toDisplayString()+"\n" }
                    )
                }
                "Monthly" -> monthlyStats?.let {
                    StatisticsContent(
                        title = "Monthly Statistics",
                        stats = it.map { it.toDisplayString()+"\n" }
                    )
                }
                "Total" -> StatisticsContent(
                    title = "Total Statistics",
                    stats = listOf(
                        "Total Time: ${TrackingUtility.formatDateTime(totalTime!!)}",
                        "Total Calories: $totalCalories",
                        "Total Distance: $totalDistance m",
                        "Average Speed: $totalSpeed km/h"
                    )
                )
            }
        }
    }
}

@Composable
fun StatisticsContent(title: String, stats: List<String>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        stats.forEach { stat ->
            Text(
                text = stat+"\n",
                style = MaterialTheme.typography.body1,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}


fun WeeklySummary.toDisplayString(): String = "Week $week $year\nDistance: $totalDistance m\nCalories: $totalCalories\nTime: ${TrackingUtility.formatDateTime(totalTime)}\nAvg Speed: $avgSpeed km/h\n"
fun MonthlySummary.toDisplayString(): String = "Month $month $year\nDistance: $totalDistance m\nCalories: $totalCalories\nTime: ${TrackingUtility.formatDateTime(totalTime)}\nAvg Speed: $avgSpeed km/h\n"
