package com.example.runtracker2.model

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.example.runtracker2.dependencyInjection.AppModule_ProvideRunDatabaseFactory
import com.example.runtracker2.model.database.MonthlySummary
import com.example.runtracker2.model.database.RunData
import com.example.runtracker2.model.database.WeeklySummary
import com.example.runtracker2.model.service.PolyLines
import com.example.runtracker2.model.service.TrackingService
import com.example.runtracker2.other.Constants.ACTION_STOP_SERVICE
import com.example.runtracker2.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.runtracker2.other.Constants.KEY_NAME
import com.example.runtracker2.other.Constants.KEY_WEIGHT
import com.example.runtracker2.other.Constants.SORTED_BY_DATE
import com.example.runtracker2.other.TrackingUtility
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.round



class Model @Inject constructor(
    private val context: Context,
    private val repositoryModel: RepositoryModel,
    private val sharedPref : SharedPreferences
) {


    private val _pathPoints = MutableStateFlow<PolyLines?>(mutableListOf())
    val pathPoints: StateFlow<PolyLines?> = _pathPoints.asStateFlow()

    private val _cameraPosition = MutableStateFlow<LatLng?>(LatLng(59.383278,18.029450))
    val cameraPosition: StateFlow<LatLng?> = _cameraPosition.asStateFlow()

    private val _cameraPositionBounds = MutableStateFlow<LatLngBounds?>(LatLngBounds.Builder().include(LatLng(59.383278,18.029450)).build())
    val cameraPositionBounds: StateFlow<LatLngBounds?> = _cameraPositionBounds.asStateFlow()

    // Weekly summaries
    private val _weeklySummaries = MutableStateFlow<List<WeeklySummary>>(emptyList())
    val weeklySummaries: StateFlow<List<WeeklySummary>> = _weeklySummaries.asStateFlow()

    // Monthly summaries
    private val _monthlySummaries = MutableStateFlow<List<MonthlySummary>>(emptyList())
    val monthlySummaries: StateFlow<List<MonthlySummary>> = _monthlySummaries.asStateFlow()


    val runDataDb : StateFlow<List<RunData>?> = repositoryModel.runData

    var curTimeInMillis = 0L

    @set:Inject
    var weight = 80f

    var isTracking = false

    private var lifecycleOwner: LifecycleOwner? = null

    fun setLifecycleOwner(owner: LifecycleOwner) {
        lifecycleOwner = owner
        observePathPoints()
        groupRunsByWeek()
        groupRunsByMonth()
    }

    private fun observePathPoints() {

        lifecycleOwner?.let { owner ->
            TrackingService.pathPoints.observe(owner, Observer { updatedPathPoints ->
                CoroutineScope(Dispatchers.IO).launch {
                    updatedPathPoints?.let {
                        _pathPoints.emit(it)
                        if(!(_pathPoints.value.isNullOrEmpty())){
                            if (_pathPoints.value!!.first().isNotEmpty()){
                                adjustCameraPosition()
                                zoomToSeeWholeTrack()
                            }
                        }


                        //Timber.d("Model: Path points.size: ${pathPoints.value?.get(0)?.size}")
                    }
                }
            })

            TrackingService.timeRunInMillis.observe(owner, Observer { updateTimeInMillis ->
                updateTimeInMillis.let {
                    curTimeInMillis = it
                }

            })
        }

    }
    fun zoomToSeeWholeTrack(){
        val bounds = LatLngBounds.Builder()
        if (!_pathPoints.value.isNullOrEmpty()) {
            for (polyline in _pathPoints.value!!) {
                for (pos in polyline) {
                    bounds.include(pos)
                }
            }

            _cameraPositionBounds.value = bounds.build()

            Timber.d("Mode: _cameraPositionBounds.value: ${_cameraPosition.value}")
        }
    }

    private fun adjustCameraPosition(){
        val bounds = LatLngBounds.Builder()
        if (!_pathPoints.value.isNullOrEmpty()) {
            for (polyline in _pathPoints.value!!) {
                for (pos in polyline) {
                    bounds.include(pos)
                }
            }
            _cameraPosition.value = bounds.build().center

            Timber.d("Mode: _cameraPosition.value: ${_cameraPosition.value}")
        }



    }


    fun sendCommandToService(action: String) {
        Intent(context, TrackingService::class.java).also {
            it.action = action
            context.startService(it)
        }
    }

    fun endRunAndSaveToDb(bitmap : Bitmap): RunData{
        var distanceInMeters = 0
        if (!_pathPoints.value.isNullOrEmpty())
            for (polyline in _pathPoints.value!!){
                distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }
            val avgSpeed = round((distanceInMeters/1000f) / (curTimeInMillis / 1000f / 60 / 60) * 10) / 10f
            val dateTimestamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters/1000f) * weight).toInt()
            val run = RunData(bitmap,dateTimestamp,avgSpeed,distanceInMeters,curTimeInMillis,caloriesBurned)
            repositoryModel.insertRun(run)
            Toast.makeText(context, "Run saved in storage", Toast.LENGTH_LONG).show()
            groupRunsByWeek()
            groupRunsByMonth()
            sendCommandToService(ACTION_STOP_SERVICE)
            return run
    }

    fun fetchDb(sortingKey : String){
        repositoryModel.fetchDb(sortingKey)
    }

    fun deleteRunFromDb( run : RunData){
        repositoryModel.deleteRun(run)
    }

    // Exposing Sorting Flows
    fun getRunsFlowSortedByDate() = repositoryModel.getRunsFlowSortedByDate()
    fun getRunsFlowSortedByTime() = repositoryModel.getRunsFlowSortedByTime()
    fun getRunsFlowSortedByCalories() = repositoryModel.getRunsFlowSortedByCalories()
    fun getRunsFlowSortedBySpeed() = repositoryModel.getRunsFlowSortedBySpeed()
    fun getRunsFlowSortedByDistance() = repositoryModel.getRunsFlowSortedByDistance()

    fun getTotalTimeInMillis() = repositoryModel.getTotalTimeInMillis()
    fun getTotalCaloriesBurned() = repositoryModel.getTotalCaloriesBurned()
    fun getTotalDistanceInMeters() = repositoryModel.getTotalDistanceInMeters()
    fun getTotalAvgSpeed() = repositoryModel.getTotalAvgSpeed()

    fun groupRunsByWeek(){

        CoroutineScope(Dispatchers.IO).launch {
            val runs = repositoryModel.fetchAndReturnDb(SORTED_BY_DATE)
            val calendar = Calendar.getInstance()

            if (runs.isNotEmpty()) {
                _weeklySummaries.value = runs.groupBy {
                    calendar.timeInMillis = it.timestampDate
                    Pair(calendar.get(Calendar.YEAR), calendar.get(Calendar.WEEK_OF_YEAR))
                }.map { (yearWeek, groupedRuns) ->
                    WeeklySummary(
                        year = yearWeek.first,
                        week = yearWeek.second,
                        totalDistance = groupedRuns.sumOf { it.distanceInMeters },
                        totalCalories = groupedRuns.sumOf { it.caloriesBurned },
                        totalTime = groupedRuns.sumOf { it.timeInMillis },
                        avgSpeed = groupedRuns.map { it.avgSpeedInKMH }.average().toFloat()
                    )
                }
            }
        }
    }


    fun groupRunsByMonth() {
        CoroutineScope(Dispatchers.IO).launch {
            val runs = repositoryModel.fetchAndReturnDb(SORTED_BY_DATE)
            val calendar = Calendar.getInstance()

            if (runs.isNotEmpty()) {
                _monthlySummaries.value = runs.groupBy {
                    calendar.timeInMillis = it.timestampDate
                    Pair(
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH) + 1
                    ) // Convert month to 1-based
                }.map { (yearMonth, groupedRuns) ->
                    MonthlySummary(
                        year = yearMonth.first,
                        month = yearMonth.second,
                        totalDistance = groupedRuns.sumOf { it.distanceInMeters },
                        totalCalories = groupedRuns.sumOf { it.caloriesBurned },
                        totalTime = groupedRuns.sumOf { it.timeInMillis },
                        avgSpeed = groupedRuns.map { it.avgSpeedInKMH }.average().toFloat()
                    )
                }
            }
        }
    }




    fun writePersonalDataToSharedPref(inputName:String, inputWeight: Float?): Boolean{
        val name = inputName
        val weight = inputWeight?.toString()?:""
        if (name.isEmpty() || weight.isEmpty()){
            Toast.makeText(context, "Fill all the fields please", Toast.LENGTH_SHORT).show()
            return false
        }
        sharedPref.edit()
            .putString(KEY_NAME, name)
            .putFloat(KEY_WEIGHT, weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE, false)
            .apply()
        return true
    }

    fun applyChangesToSharedPref(inputName:String, inputWeight: Float?): Boolean {
        val name = inputName
        val weight = inputWeight?.toString()?:""
        if (name.isEmpty() || weight.isEmpty()){
            Toast.makeText(context, "Fill all the fields please", Toast.LENGTH_SHORT).show()
            return false
        }
        sharedPref.edit()
            .putString(KEY_NAME, name)
            .putFloat(KEY_WEIGHT, weight.toFloat())
            .apply()
        return true
    }

    fun loadFieldsFromSharedPref() : Pair<String,String>{
        val infoPair = Pair(sharedPref.getString(KEY_NAME,"")?:"",(sharedPref.getFloat(KEY_WEIGHT,80f)).toString())
        return infoPair
    }

}