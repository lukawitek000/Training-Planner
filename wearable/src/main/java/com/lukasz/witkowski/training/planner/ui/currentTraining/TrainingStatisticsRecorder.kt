package com.lukasz.witkowski.training.planner.ui.currentTraining

import androidx.concurrent.futures.await
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.ExerciseUpdateListener
import androidx.health.services.client.data.AggregateDataPoint
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.CumulativeDataPoint
import androidx.health.services.client.data.DataPoint
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseCapabilities
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseState
import androidx.health.services.client.data.ExerciseTrackedStatus
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseUpdate
import androidx.health.services.client.data.StatisticalDataPoint
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lukasz.witkowski.shared.models.statistics.CaloriesStatistics
import com.lukasz.witkowski.shared.models.statistics.ExerciseStatistics
import com.lukasz.witkowski.shared.models.statistics.HeartRateStatistics
import com.lukasz.witkowski.shared.models.statistics.TrainingCompleteStatistics
import com.lukasz.witkowski.shared.models.statistics.TrainingStatistics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class TrainingStatisticsRecorder @Inject constructor(
    private val exerciseClient: ExerciseClient
) {

    private val coroutineScope =
        CoroutineScope(Dispatchers.Main + Job()) // Job makes coroutine cancellable

    private var exerciseConfig: ExerciseConfig? = null

    private val isExerciseConfigured: Boolean
        get() = exerciseConfig != null

    private var exerciseState = ExerciseState.USER_ENDED

    private var exercisesIdAndSetQueue: Queue<Pair<Long, Int>> = LinkedList()
    private var isLastExercise: Queue<Boolean> = LinkedList()
    var trainingCompleteStatistics: TrainingCompleteStatistics? = null
        private set

    private val _isHeartRateSupported = MutableLiveData(true)
    val isHeartRateSupported: LiveData<Boolean> = _isHeartRateSupported

    private val _isBurntKcalSupported = MutableLiveData(true)
    val isBurntKcalSupported: LiveData<Boolean> = _isBurntKcalSupported

    private val _isWorkoutExerciseSupported = MutableLiveData(true)
    val isWorkoutExerciseSupported: LiveData<Boolean> = _isWorkoutExerciseSupported

    private val _exerciseUpdatesEndedMessage = MutableLiveData("")
    val exerciseUpdatesEndedMessage: LiveData<String> = _exerciseUpdatesEndedMessage

    private val _isTrainingEnded = MutableStateFlow(false)
    val isTrainingEnded: StateFlow<Boolean> = _isTrainingEnded

    private var isLastExerciseStatisticsSaved = false

    private var caloriesCumulativeData: CumulativeDataPoint? = null
    private var heartRateStatisticalData: StatisticalDataPoint? = null
    private val heartRateDuringTraining = mutableListOf<Double>()
    private var startTrainingTime = 0L
    val trainingTime: Long
        get() = System.currentTimeMillis() - startTrainingTime



    fun initTrainingStatistics(trainingId: Long) {
        startTrainingTime = System.currentTimeMillis()
        trainingCompleteStatistics = TrainingCompleteStatistics(
            trainingStatistics = TrainingStatistics(
                trainingId = trainingId,
                date = System.currentTimeMillis()
            ),
            exercisesStatistics = emptyList()
        )
    }

    fun startRecordingExerciseStatistics(exerciseId: Long, currentSet: Int) {
        exercisesIdAndSetQueue.offer(Pair(exerciseId, currentSet))
        monitorHealthIndicators()
    }

    private fun saveTrainingStatistics(trainingTime: Long) {
        trainingCompleteStatistics?.trainingStatistics?.totalTime = trainingTime
        trainingCompleteStatistics?.trainingStatistics?.heartRateHistory = heartRateDuringTraining
    }

    fun finishExercise() {
        coroutineScope.launch {
            endExercise()
        }
    }

    fun cancelScope() {
        coroutineScope.cancel()
    }

    private fun monitorHealthIndicators() {
        coroutineScope.launch {
            endExercise()
            if (!isExerciseConfigured) {
                configureHealthServices()
            }
            exerciseConfig?.let {
                exerciseClient.startExercise(it).await()
            }
        }
    }

    private suspend fun endExercise() {
        if (isExerciseInProgress()) {
            exerciseClient.endExercise().await()
        }
    }

    private suspend fun isExerciseInProgress(): Boolean {
        val exerciseInfo = exerciseClient.currentExerciseInfo.await()
        return exerciseInfo.exerciseTrackedStatus == ExerciseTrackedStatus.OWNED_EXERCISE_IN_PROGRESS
    }

    private val exerciseUpdateListener = object : ExerciseUpdateListener {
        override fun onAvailabilityChanged(dataType: DataType, availability: Availability) = Unit

        override fun onExerciseUpdate(update: ExerciseUpdate) {
            processExerciseUpdate(update)
        }

        override fun onLapSummary(lapSummary: ExerciseLapSummary) = Unit
    }

    private suspend fun configureHealthServices() {
        val capabilities = exerciseClient.capabilities.await()
        val exerciseType = ExerciseType.WORKOUT
        if (exerciseType in capabilities.supportedExerciseTypes) {
            if(configureExerciseCapabilities(capabilities, exerciseType)) {
                configureExercise(exerciseType)
                exerciseClient.setUpdateListener(exerciseUpdateListener)
            }
        } else {
            _isWorkoutExerciseSupported.value = false
        }
    }

    private fun configureExercise(exerciseType: ExerciseType) {
        val aggregateDataTypes = setOf(
            DataType.TOTAL_CALORIES,
            DataType.HEART_RATE_BPM
        )
        val dataTypes = setOf(
            DataType.HEART_RATE_BPM
        )
        exerciseConfig = ExerciseConfig.builder()
            .setExerciseType(exerciseType)
            .setAggregateDataTypes(aggregateDataTypes)
            .setDataTypes(dataTypes)
            .build()
    }

    private fun configureExerciseCapabilities(
        capabilities: ExerciseCapabilities,
        exerciseType: ExerciseType
    ): Boolean {
        val exerciseCapabilities = capabilities.getExerciseTypeCapabilities(exerciseType)
        _isHeartRateSupported.value =
            DataType.HEART_RATE_BPM in exerciseCapabilities.supportedDataTypes
        _isBurntKcalSupported.value =
            DataType.TOTAL_CALORIES in exerciseCapabilities.supportedDataTypes
        return isHeartRateSupported.value!! || isBurntKcalSupported.value!!
    }

    private fun processExerciseUpdate(exerciseUpdate: ExerciseUpdate) {
        val oldState = exerciseState
        if (!oldState.isEnded && exerciseUpdate.state.isEnded) {
            // Exercise ended
            val exerciseTime = exerciseUpdate.activeDuration.toMillis()
            saveRecordedHealthStatistics(exerciseTime)
            handleDifferentEndCauses(exerciseUpdate)
            clearRecordedHealthStatistics()
        }
        exerciseState = exerciseUpdate.state
        val aggregatedMetrics = exerciseUpdate.latestAggregateMetrics
        val latestMetrics = exerciseUpdate.latestMetrics

        saveLatestMetrics(aggregatedMetrics, latestMetrics)
    }

    private fun clearRecordedHealthStatistics() {
        caloriesCumulativeData = null
        heartRateStatisticalData = null
    }

    private fun saveLatestMetrics(
        aggregatedMetrics: Map<DataType, AggregateDataPoint>,
        latestMetrics: Map<DataType, List<DataPoint>>
    ) {
        caloriesCumulativeData =
            (aggregatedMetrics[DataType.TOTAL_CALORIES] as? CumulativeDataPoint)
                ?: caloriesCumulativeData
        heartRateStatisticalData =
            (aggregatedMetrics[DataType.HEART_RATE_BPM] as? StatisticalDataPoint)
                ?: heartRateStatisticalData
        latestMetrics[DataType.HEART_RATE_BPM]?.let {
            saveCurrentHeartRate(it)
        }
    }

    private fun saveCurrentHeartRate(it: List<DataPoint>) {
        try {
            val heartRate = it.last().value.asDouble()
            if (heartRate != 0.0) {
                heartRateDuringTraining.add(heartRate)
            }
        } catch (e: IllegalStateException) {
            Timber.w("Heart rate is not double")
        }
    }

    private fun handleDifferentEndCauses(exerciseUpdate: ExerciseUpdate) {
        when (exerciseUpdate.state) {
            ExerciseState.TERMINATED -> {
                // Another app started tracking an exercise
                _exerciseUpdatesEndedMessage.value = "Another app terminated exercise"
            }
            ExerciseState.AUTO_ENDED -> {
                // The exercise was auto ended because there were no registered listeners
                _exerciseUpdatesEndedMessage.value = "No registered listener"
            }
            ExerciseState.AUTO_ENDED_PERMISSION_LOST -> {
                // Your exercises ended because it lost the required permissions
                _exerciseUpdatesEndedMessage.value = "Required permissions lost"
            }
            ExerciseState.USER_ENDED -> {
                // User ended exercise
            }
            else -> {}
        }
    }

    private fun saveRecordedHealthStatistics(
        exerciseTime: Long
    ) {
        if (trainingCompleteStatistics == null) return
        val exercisesStatistics = trainingCompleteStatistics!!.exercisesStatistics.toMutableList()
        val currentCaloriesStatistics = getCaloriesStatistics()
        val currentHeartRateStatistics = getHeartRateStatistics()
        val (currentExerciseId, currentSet) = exercisesIdAndSetQueue.poll() ?: return
        val isCurrentLastExercise = isLastExercise.poll() ?: return
        if (hasCurrentExerciseStatistics(exercisesStatistics, currentExerciseId)) {
            updateStatisticsInTheList(
                currentExerciseId,
                exercisesStatistics,
                currentHeartRateStatistics,
                currentSet,
                currentCaloriesStatistics,
                exerciseTime
            )
        } else {
            addExerciseStatistics(
                currentExerciseId,
                currentHeartRateStatistics,
                currentCaloriesStatistics,
                exerciseTime,
                exercisesStatistics
            )
        }
        trainingCompleteStatistics!!.exercisesStatistics = exercisesStatistics
        informAboutFinishedTraining(isCurrentLastExercise)
    }

    private fun informAboutFinishedTraining(isLastExercise: Boolean) {
        if(!_isTrainingEnded.value && isLastExercise) {
            saveTrainingStatistics(trainingTime)
            if(trainingCompleteStatistics!!.trainingStatistics.totalTime > 0L) {
                _isTrainingEnded.value = true
                _isTrainingEnded.value = false
            }
        }
    }

    private fun hasCurrentExerciseStatistics(
        exercisesStatistics: MutableList<ExerciseStatistics>,
        currentExerciseId: Long
    ) = exercisesStatistics.any { it.trainingExerciseId == currentExerciseId }

    private fun addExerciseStatistics(
        currentExerciseId: Long,
        currentHeartRateStatistics: HeartRateStatistics,
        currentCaloriesStatistics: CaloriesStatistics,
        exerciseTime: Long,
        exercisesStatistics: MutableList<ExerciseStatistics>
    ) {
        val exerciseStatistics = ExerciseStatistics(
            trainingExerciseId = currentExerciseId,
            heartRateStatistics = currentHeartRateStatistics,
            burntCaloriesStatistics = currentCaloriesStatistics,
            averageTime = exerciseTime
        )
        exercisesStatistics.add(exerciseStatistics)
    }

    private fun updateStatisticsInTheList(
        currentExerciseId: Long,
        exercisesStatistics: MutableList<ExerciseStatistics>,
        currentHeartRateStatistics: HeartRateStatistics,
        currentSet: Int,
        currentCaloriesStatistics: CaloriesStatistics,
        exerciseTime: Long
    ) {
        val index = exercisesStatistics.indexOfFirst { it.trainingExerciseId == currentExerciseId }
        val savedSets = if (currentSet == 1) 1 else currentSet - 1
        val exerciseStatistics = exercisesStatistics[index]
        val heartRateStatistics = updateExerciseHeartRateStatistics(
            currentHeartRateStatistics,
            exerciseStatistics,
            savedSets,
            currentSet
        )
        val caloriesStatistics =
            updateCaloriesStatistics(exerciseStatistics, currentCaloriesStatistics)
        val averageTime = updateAverageTime(exerciseStatistics, savedSets, exerciseTime, currentSet)
        exercisesStatistics.removeAt(index)
        exercisesStatistics.add(
            ExerciseStatistics(
                id = exerciseStatistics.id,
                trainingExerciseId = exerciseStatistics.trainingExerciseId,
                heartRateStatistics = heartRateStatistics,
                burntCaloriesStatistics = caloriesStatistics,
                averageTime = averageTime
            )
        )
    }

    private fun updateAverageTime(
        exerciseStatistics: ExerciseStatistics,
        savedSets: Int,
        exerciseTime: Long,
        currentSet: Int
    ) = (exerciseStatistics.averageTime * savedSets + exerciseTime) / currentSet

    private fun updateCaloriesStatistics(
        exerciseStatistics: ExerciseStatistics,
        currentCaloriesStatistics: CaloriesStatistics
    ) = CaloriesStatistics(
        burntCalories = exerciseStatistics.burntCaloriesStatistics.burntCalories + currentCaloriesStatistics.burntCalories
    )

    private fun updateExerciseHeartRateStatistics(
        currentHeartRateStatistics: HeartRateStatistics,
        exerciseStatistics: ExerciseStatistics,
        savedSets: Int,
        currentSet: Int
    ): HeartRateStatistics {
        return HeartRateStatistics(
            max = max(currentHeartRateStatistics.max, exerciseStatistics.heartRateStatistics.max),
            min = min(currentHeartRateStatistics.min, exerciseStatistics.heartRateStatistics.min),
            average = (currentHeartRateStatistics.average * savedSets + exerciseStatistics.heartRateStatistics.average) / currentSet
        )
    }

    private fun getCaloriesStatistics() =
        CaloriesStatistics(caloriesCumulativeData?.total?.asDouble() ?: 0.0)

    private fun getHeartRateStatistics() = HeartRateStatistics(
        max = heartRateStatisticalData?.max?.asDouble() ?: 0.0,
        min = heartRateStatisticalData?.min?.asDouble() ?: 0.0,
        average = heartRateStatisticalData?.average?.asDouble() ?: 0.0
    )

    fun setLastExercise(isLastExercise: Boolean) {
        this.isLastExercise.offer(isLastExercise)
    }
}
