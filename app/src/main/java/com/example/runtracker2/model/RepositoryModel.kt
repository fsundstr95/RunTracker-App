package com.example.runtracker2.model

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.example.runtracker2.model.database.RunDAO
import com.example.runtracker2.model.database.RunData
import com.example.runtracker2.other.Constants.SORTED_BY_AVG_SPEED
import com.example.runtracker2.other.Constants.SORTED_BY_CALORIES_BURNED
import com.example.runtracker2.other.Constants.SORTED_BY_DATE
import com.example.runtracker2.other.Constants.SORTED_BY_DISTANCE
import com.example.runtracker2.other.Constants.SORTED_BY_TIME_IN_MILLIS
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject


class RepositoryModel @Inject constructor( val runDao : RunDAO) {

    private val _runData = MutableStateFlow<List<RunData>?>(emptyList())
    val runData: StateFlow<List<RunData>?> = _runData.asStateFlow()


    // Reactive Streams
    fun getRunsFlowSortedByDate(): Flow<List<RunData>> = runDao.getAllRunsFlowSortedByDate()
    fun getRunsFlowSortedByTime(): Flow<List<RunData>> = runDao.getAllRunsFlowSortedByTimeInMillis()
    fun getRunsFlowSortedByCalories(): Flow<List<RunData>> = runDao.getAllRunsFlowSortedByCaloriesBurned()
    fun getRunsFlowSortedBySpeed(): Flow<List<RunData>> = runDao.getAllRunsFlowSortedByAvgSpeed()
    fun getRunsFlowSortedByDistance(): Flow<List<RunData>> = runDao.getAllRunsFlowSortedByDistance()

    fun getTotalTimeInMillis(): Flow<Long?> = runDao.getTotalTimeInMillis()
    fun getTotalCaloriesBurned(): Flow<Int?> = runDao.getTotalCaloriesBurned()
    fun getTotalDistanceInMeters(): Flow<Int?> = runDao.getTotalDistanceInMeters()
    fun getTotalAvgSpeed(): Flow<Float?> = runDao.getTotalAvgSpeed()



    fun insertRun(run: RunData){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                runDao.insert(run)
            } catch (e: SQLiteConstraintException) {
                Log.e("DatabaseError", "Conflict while inserting data: ${e.message}")
                println("Conflict while inserting data: ${e.message}")

            } catch (e: Exception) {
                Log.e("DatabaseError", "Failed to insert data: ${e.message}")
                println("Failed to insert data: ${e.message}")
            }


        }

    }

    fun fetchDb(sortingKey : String){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when(sortingKey){
                    SORTED_BY_DATE -> _runData.value = runDao.getAllRunsSortedByDate()
                    SORTED_BY_TIME_IN_MILLIS -> _runData.value = runDao.getAllRunsSortedByTimeInMillis()
                    SORTED_BY_CALORIES_BURNED -> _runData.value = runDao.getAllRunsSortedByCaloriesBurned()
                    SORTED_BY_AVG_SPEED -> _runData.value = runDao.getAllRunsSortedByAvgSpeed()
                    SORTED_BY_DISTANCE -> _runData.value = runDao.getAllRunsSortedByDistance()
                }

            } catch (e: SQLiteConstraintException) {
                Log.e("DatabaseError", "Conflict while fetching data: ${e.message}")
                println("Conflict while fetching data: ${e.message}")

            } catch (e: Exception) {
                Log.e("DatabaseError", "Failed to fetch data: ${e.message}")
                println("Failed to fetch data: ${e.message}")
            }
        }

    }

    suspend fun fetchAndReturnDb(sortingKey: String): List<RunData> {
        return withContext(Dispatchers.IO) {
            try {
                when (sortingKey) {
                    SORTED_BY_DATE -> runDao.getAllRunsSortedByDate()
                    SORTED_BY_TIME_IN_MILLIS -> runDao.getAllRunsSortedByTimeInMillis()
                    SORTED_BY_CALORIES_BURNED -> runDao.getAllRunsSortedByCaloriesBurned()
                    SORTED_BY_AVG_SPEED -> runDao.getAllRunsSortedByAvgSpeed()
                    SORTED_BY_DISTANCE -> runDao.getAllRunsSortedByDistance()
                    else -> emptyList() // Handle unknown sorting key
                }
            } catch (e: SQLiteConstraintException) {
                Log.e("DatabaseError", "Conflict while fetching data: ${e.message}")
                emptyList()
            } catch (e: Exception) {
                Log.e("DatabaseError", "Failed to fetch data: ${e.message}")
                emptyList()
            }
        }
    }




    fun deleteRun(run: RunData){

        CoroutineScope(Dispatchers.IO).launch {
            try {
                runDao.deleteRun(run)
            } catch (e: SQLiteConstraintException) {
                Log.e("DatabaseError", "Conflict while deleting data: ${e.message}")
                println("Conflict while deleting data: ${e.message}")

            } catch (e: Exception) {
                Log.e("DatabaseError", "Failed to delete data: ${e.message}")
                println("Failed to delete data: ${e.message}")
            }
        }
    }

}