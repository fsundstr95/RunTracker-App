package com.example.runtracker2.model.database

data class WeeklySummary(
    val year: Int,
    val week: Int,
    val totalDistance: Int,
    val totalCalories: Int,
    val totalTime: Long,
    val avgSpeed: Float
)

data class MonthlySummary(
    val year: Int,
    val month: Int,
    val totalDistance: Int,
    val totalCalories: Int,
    val totalTime: Long,
    val avgSpeed: Float
)


