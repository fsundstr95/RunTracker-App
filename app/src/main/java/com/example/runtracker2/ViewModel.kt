package com.example.runtracker2

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.runtracker2.model.Model
import com.example.runtracker2.model.database.MonthlySummary
import com.example.runtracker2.model.database.RunData
import com.example.runtracker2.model.database.WeeklySummary
import com.example.runtracker2.model.service.PolyLines
import com.example.runtracker2.model.service.TrackingService
import com.example.runtracker2.other.Constants.ACTION_STOP_SERVICE
import com.example.runtracker2.other.Constants.SORTED_BY_AVG_SPEED
import com.example.runtracker2.other.Constants.SORTED_BY_CALORIES_BURNED
import com.example.runtracker2.other.Constants.SORTED_BY_DATE
import com.example.runtracker2.other.Constants.SORTED_BY_DISTANCE
import com.example.runtracker2.other.Constants.SORTED_BY_TIME_IN_MILLIS
import com.example.runtracker2.other.TrackingUtility
import com.example.runtracker2.views.MapState
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber


class ViewModel(private val model: Model, private val lifecycleOwner: LifecycleOwner): ViewModel() {

    val state by mutableStateOf(MapState())

    // StateFlow to observe path points updates

    private val _pathPoints = MutableStateFlow<PolyLines?>(null)
    val pathPoints: StateFlow<PolyLines?> = _pathPoints

    private val _pathPoints2 = MutableStateFlow<PolyLines?>(null)
    val pathPoints2: StateFlow<PolyLines?> = _pathPoints2

    private val _runTime = MutableStateFlow<String?>("")
    val runTime : StateFlow<String?> = _runTime

    private val _isTracking = MutableStateFlow<Boolean?>(false)
    val isTracking : StateFlow<Boolean?> = _isTracking


    val cameraPosition: StateFlow<LatLng?> = model.cameraPosition
    val cameraPositionBounds: StateFlow<LatLngBounds?> = model.cameraPositionBounds
    val runDataDb : StateFlow<List<RunData>?> = model.runDataDb


    val weeklyRunDataFlow: StateFlow<List<WeeklySummary>?> = model.weeklySummaries

    val monthlyRunDataFlow: StateFlow<List<MonthlySummary>?> = model.monthlySummaries

    private val _lastStoredRun = MutableStateFlow<RunData?>(null)
    val lastStoredRun : StateFlow<RunData?> = _lastStoredRun.asStateFlow()

    private val _lastPathPoints = MutableStateFlow<PolyLines?>(null)
    val lastPathPoints: StateFlow<PolyLines?> = _lastPathPoints

    private val _lastCameraPositionBounds = MutableStateFlow<LatLngBounds?>(LatLngBounds.Builder().include(LatLng(59.383278,18.029450)).build())
    val lastCameraPositionBounds: StateFlow<LatLngBounds?> = _lastCameraPositionBounds.asStateFlow()

    private val _storedRunFlow = MutableStateFlow<List<RunData?>>(emptyList())
    val storedRunFlow : StateFlow<List<RunData?>> = _storedRunFlow.asStateFlow()

    private val _sortingKey = MutableStateFlow(SORTED_BY_DATE)
    val sortingKey: StateFlow<String> = _sortingKey


    private val _totalTimeInMillisFlow = MutableStateFlow<Long?>(null)
    val totalTimeInMillisFlow: StateFlow<Long?> = _totalTimeInMillisFlow.asStateFlow()

    private val _totalCaloriesBurnedFlow = MutableStateFlow<Int?>(null)
    val totalCaloriesBurnedFlow: StateFlow<Int?> = _totalCaloriesBurnedFlow.asStateFlow()

    private val _totalDistanceInMetersFlow = MutableStateFlow<Int?>(null)
    val totalDistanceInMetersFlow: StateFlow<Int?> = _totalDistanceInMetersFlow.asStateFlow()

    private val _totalAvgSpeedFlow = MutableStateFlow<Float?>(null)
    val totalAvgSpeedFlow: StateFlow<Float?> = _totalAvgSpeedFlow.asStateFlow()

    init {

        observePathPoints()
        fetchSortedRuns(SORTED_BY_DATE)
        collectStatisticsFlows()



    }

    fun changeSortingKey(key: String) {
        _sortingKey.value = key
        fetchSortedRuns(key)
    }

    private fun fetchSortedRuns(sortingKey: String) {
        viewModelScope.launch {

            val sortedFlow = when (sortingKey) {
                SORTED_BY_DATE -> model.getRunsFlowSortedByDate()
                SORTED_BY_TIME_IN_MILLIS -> model.getRunsFlowSortedByTime()
                SORTED_BY_CALORIES_BURNED -> model.getRunsFlowSortedByCalories()
                SORTED_BY_AVG_SPEED -> model.getRunsFlowSortedBySpeed()
                SORTED_BY_DISTANCE -> model.getRunsFlowSortedByDistance()
                else -> model.getRunsFlowSortedByDate()
            }

            sortedFlow.collect { runs ->
                _storedRunFlow.value = runs
            }
        }
    }

    fun collectStatisticsFlows() {
        viewModelScope.launch {
            launch {
                model.getTotalTimeInMillis().collect { value ->
                    _totalTimeInMillisFlow.value = value ?: 0L
                }
            }
            launch {
                model.getTotalCaloriesBurned().collect { value ->
                    _totalCaloriesBurnedFlow.value = value ?: 0
                }
            }
            launch {
                model.getTotalDistanceInMeters().collect { value ->
                    _totalDistanceInMetersFlow.value = value ?: 0
                }
            }
            launch {
                model.getTotalAvgSpeed().collect { value ->
                    _totalAvgSpeedFlow.value = value ?: 0f
                }
            }
        }
    }



    private fun observePathPoints() {

        lifecycleOwner.let { owner ->
            TrackingService.pathPoints.observe(owner, Observer { updatedPathPoints ->
                updatedPathPoints?.let {
                    _pathPoints2.value = it
                    //Timber.d("VM: _pathPoints2.size: ${pathPoints2.value?.get(0)?.size}")
                }

            })

            TrackingService.timeRunInMillis.observe(owner, Observer{ updateRunTime ->
                updateRunTime?.let {
                    _runTime.value = TrackingUtility.getFormattedStopWatchTime(it,true)
                }

            })

            TrackingService.isTracking.observe(owner, Observer { updateIsTracking ->
                updateIsTracking?.let { _isTracking.value = it }
            })
        }

    }

    fun sendCommandToService(action : String){
        model.sendCommandToService(action)
    }

    fun saveRun(bitmap: Bitmap)  {
        _lastCameraPositionBounds.value = cameraPositionBounds.value
        _lastPathPoints.value = _pathPoints2.value
        _lastStoredRun.value  = model.endRunAndSaveToDb(bitmap)
        Timber.d("Save run!")

    }

    fun fetchDb(sortingKey : String){
        model.fetchDb(sortingKey)
    }

    fun deleteRun(){
        model.sendCommandToService(ACTION_STOP_SERVICE)
        Timber.d("Delete run!")
    }

    fun deleteRunFromDb( run : RunData){
        model.deleteRunFromDb(run)
    }

    fun writePersonalDataToSharedPref(inputName:String, inputWeight: Float?): Boolean{
        return model.writePersonalDataToSharedPref(inputName,inputWeight)
    }
    fun applyChangesToSharedPref(inputName:String, inputWeight: Float?): Boolean {
        return model.applyChangesToSharedPref(inputName  ,inputWeight)
    }

    fun loadFieldsFromSharedPref() : Pair<String,String>{
        return model.loadFieldsFromSharedPref()
    }
}