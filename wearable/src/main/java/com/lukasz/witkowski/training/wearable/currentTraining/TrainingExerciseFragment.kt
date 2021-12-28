package com.lukasz.witkowski.training.wearable.currentTraining

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.concurrent.futures.await
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.health.services.client.ExerciseClient
import androidx.health.services.client.ExerciseUpdateListener
import androidx.health.services.client.HealthServices
import androidx.health.services.client.data.Availability
import androidx.health.services.client.data.DataType
import androidx.health.services.client.data.ExerciseConfig
import androidx.health.services.client.data.ExerciseLapSummary
import androidx.health.services.client.data.ExerciseType
import androidx.health.services.client.data.ExerciseTypeCapabilities
import androidx.health.services.client.data.ExerciseUpdate
import androidx.lifecycle.lifecycleScope
import com.lukasz.witkowski.shared.models.TrainingExercise
import com.lukasz.witkowski.shared.utils.TimeFormatter
import com.lukasz.witkowski.training.wearable.R
import com.lukasz.witkowski.training.wearable.currentTraining.service.TrainingService
import com.lukasz.witkowski.training.wearable.databinding.FragmentTrainingExerciseBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class TrainingExerciseFragment : Fragment() {

    private lateinit var binding: FragmentTrainingExerciseBinding
//    private val viewModel: CurrentTrainingViewModel by activityViewModels()
    private val timerViewModel: TimerViewModel by viewModels()

    private lateinit var trainingService: TrainingService

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            trainingService = (service as TrainingService.LocalBinder).getService()
            observeCurrentExercise()
            setPlayButtonListener()
            setNextExerciseButtonListener()
            observeExerciseTimer()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Timber.d("Service disconnected")
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrainingExerciseBinding.inflate(inflater, container, false)
//        observeCurrentExercise()
//        setPlayButtonListener()
//        setNextExerciseButtonListener()
//        observeExerciseTimer()

        val serviceIntent = Intent(requireContext(), TrainingService::class.java)
        requireActivity().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unbindService(connection)
    }

    val exerciseUpdateListener = object : ExerciseUpdateListener {
        override fun onAvailabilityChanged(dataType: DataType, availability: Availability) {
            Timber.d("Availability changed $dataType $availability")
        }

        override fun onExerciseUpdate(update: ExerciseUpdate) {
            Timber.d("On exercise update $update")
        }

        override fun onLapSummary(lapSummary: ExerciseLapSummary) {
            Timber.d("On lap summary $lapSummary")
        }

    }

    private lateinit var exerciseClient: ExerciseClient

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val healthClient = HealthServices.getClient(requireContext())
        exerciseClient = healthClient.exerciseClient
        var exerciseCapabilities: ExerciseTypeCapabilities? = null
        lifecycleScope.launch {
            val capabilities = exerciseClient.capabilities.await()
            val exerciseType = ExerciseType.CALISTHENICS
            Timber.d("Exercise type $exerciseType")
            if(exerciseType in capabilities.supportedExerciseTypes) {
                Timber.d("Exercise type is in capabilities supported types ${capabilities.supportedExerciseTypes}")
                exerciseCapabilities = capabilities.getExerciseTypeCapabilities(exerciseType)

                exerciseClient.setUpdateListener(exerciseUpdateListener)
                // Types for which we want to receive metrics.
                val dataTypes = setOf(
                    DataType.HEART_RATE_BPM
                )
                // Types for which we want to receive aggregate metrics.
                val aggregateDataTypes = setOf(
                    // "Total" here refers not to the aggregation but to basal + activity.
                    DataType.TOTAL_CALORIES,
                    DataType.HEART_RATE_BPM
                )
                val config = ExerciseConfig.builder()
                    .setExerciseType(exerciseType)
                    .setDataTypes(dataTypes)
                    .setAggregateDataTypes(aggregateDataTypes)
                    .build()
                exerciseClient.startExercise(config).await()
            }
        }

    }

    private fun observeExerciseTimer() {
        timerViewModel.timeLeft.observe(viewLifecycleOwner) {
            binding.timerTv.text = TimeFormatter.millisToTimer(it)
        }
        timerViewModel.timerFinished.observe(viewLifecycleOwner) {
            if (it) {
                setTimerButtonIcon(isTimerRunning = false)
                // TODO Some sound / vibration that timer has finished
            }
        }
    }

    private fun setNextExerciseButtonListener() {
        binding.nextBtn.setOnClickListener {
//            viewModel.navigateToTrainingRestTime()
            trainingService.currentTrainingProgressHelper.navigateToTrainingRestTime()
            timerViewModel.cancelTimer()
            exerciseClient.endExercise()
        }
    }

    private fun setPlayButtonListener() {
        binding.startPauseTimerBtn.setOnClickListener {
            if (!timerViewModel.isRunning && !timerViewModel.isPaused) {
//                timerViewModel.startTimer(viewModel.exerciseTime)
                timerViewModel.startTimer(trainingService.currentTrainingProgressHelper.exerciseTime)
            } else if (timerViewModel.isPaused) {
                timerViewModel.resumeTimer()
            } else if (timerViewModel.isRunning) {
                timerViewModel.pauseTimer()
            }
            setTimerButtonIcon(timerViewModel.isRunning)
        }
    }

    private fun observeCurrentExercise() {
//        viewModel.currentExercise.observe(viewLifecycleOwner) {
//            setExerciseDataToUi(it)
//        }
        trainingService.currentTrainingProgressHelper.currentTrainingState.observe(viewLifecycleOwner) {
            Timber.d("Training state changed $it")
            if(it is CurrentTrainingState.ExerciseState) {
                setExerciseDataToUi(it.exercise)
            }
        }
    }

    private fun setExerciseDataToUi(trainingExercise: TrainingExercise) {
        binding.exerciseNameTv.text = trainingExercise.exercise.name
        binding.repetitionsTv.text = getString(R.string.reps_text, trainingExercise.repetitions)
        binding.setsTv.text = getString(R.string.sets_text, trainingExercise.sets)
        val time = trainingExercise.time
        binding.apply {
            if (time == 0L) {
                timerTv.visibility = View.GONE
                startPauseTimerBtn.visibility = View.GONE
            } else {
                timerTv.visibility = View.VISIBLE
                startPauseTimerBtn.visibility = View.VISIBLE
                timerTv.text = TimeFormatter.millisToTimer(time)
            }
        }
        setTimerButtonIcon()
    }

    private fun setTimerButtonIcon(isTimerRunning: Boolean = false) {
        val icon = if (isTimerRunning) R.drawable.ic_pause else R.drawable.ic_play_arrow
        binding.startPauseTimerBtn.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                icon
            )
        )
    }
}