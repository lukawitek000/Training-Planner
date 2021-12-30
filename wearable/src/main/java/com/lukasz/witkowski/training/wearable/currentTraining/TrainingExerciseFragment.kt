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
import com.lukasz.witkowski.training.wearable.currentTraining.service.TimerHelper
import com.lukasz.witkowski.training.wearable.currentTraining.service.TrainingService
import com.lukasz.witkowski.training.wearable.databinding.FragmentTrainingExerciseBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class TrainingExerciseFragment : Fragment() {

    private lateinit var binding: FragmentTrainingExerciseBinding
//    private val viewModel: CurrentTrainingViewModel by activityViewModels()
//    private val timerViewModel: TimerViewModel by viewModels()

    private lateinit var trainingService: TrainingService

    private lateinit var timer: TimerHelper

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            trainingService = (service as TrainingService.LocalBinder).getService()
            timer = trainingService.timerHelper
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



    private fun observeExerciseTimer() {
//        timerViewModel.timeLeft.observe(viewLifecycleOwner) {
//            binding.timerTv.text = TimeFormatter.millisToTimer(it)
//        }
//        timerViewModel.timerFinished.observe(viewLifecycleOwner) {
//            if (it) {
//                setTimerButtonIcon(isTimerRunning = false)
//                // TODO Some sound / vibration that timer has finished
//            }
//        }
        timer.timeLeft.observe(viewLifecycleOwner) {
            if(trainingService.currentTrainingProgressHelper.isExerciseState && timer.isRunning) {
                binding.timerTv.text = TimeFormatter.millisToTimer(it)
            }
        }
        timer.timerFinished.observe(viewLifecycleOwner) {
            if (it) {
                setTimerButtonIcon(isTimerRunning = false)
                // TODO Some sound / vibration that timer has finished
            }
        }
    }

    private fun setNextExerciseButtonListener() {
        binding.nextBtn.setOnClickListener {
//            viewModel.navigateToTrainingRestTime()
            timer.cancelTimer()
            trainingService.currentTrainingProgressHelper.navigateToTrainingRestTime()
//            timerViewModel.cancelTimer()

//            exerciseClient.endExercise()
        }
    }

    private fun setPlayButtonListener() {
        binding.startPauseTimerBtn.setOnClickListener {
//            if (!timerViewModel.isRunning && !timerViewModel.isPaused) {
////                timerViewModel.startTimer(viewModel.exerciseTime)
//                timerViewModel.startTimer(trainingService.currentTrainingProgressHelper.exerciseTime)
//            } else if (timerViewModel.isPaused) {
//                timerViewModel.resumeTimer()
//            } else if (timerViewModel.isRunning) {
//                timerViewModel.pauseTimer()
//            }
//            setTimerButtonIcon(timerViewModel.isRunning)
            if (!timer.isRunning && !timer.isPaused) {
//                timerViewModel.startTimer(viewModel.exerciseTime)
                timer.startTimer(trainingService.currentTrainingProgressHelper.exerciseTime)
            } else if (timer.isPaused) {
                timer.resumeTimer()
            } else if (timer.isRunning) {
                timer.pauseTimer()
            }
            setTimerButtonIcon(timer.isRunning)
        }
    }

    private fun observeCurrentExercise() {
//        viewModel.currentExercise.observe(viewLifecycleOwner) {
//            setExerciseDataToUi(it)
//        }
        trainingService.currentTrainingProgressHelper.currentTrainingState.observe(viewLifecycleOwner) {
            Timber.d("Training state changed $it")
            if(it is CurrentTrainingState.ExerciseState) {
                setExerciseDataToUi(it.exercise, trainingService.currentTrainingProgressHelper.exerciseTime)
            }
        }
    }

    private fun setExerciseDataToUi(trainingExercise: TrainingExercise, exerciseTime: Long) {
        binding.exerciseNameTv.text = trainingExercise.exercise.name
        binding.repetitionsTv.text = getString(R.string.reps_text, trainingExercise.repetitions)
        binding.setsTv.text = getString(R.string.sets_text, trainingExercise.sets)
        binding.apply {
            if (exerciseTime == 0L) {
                timerTv.visibility = View.GONE
                startPauseTimerBtn.visibility = View.GONE
            } else {
                timerTv.visibility = View.VISIBLE
                startPauseTimerBtn.visibility = View.VISIBLE
                timerTv.text = TimeFormatter.millisToTimer(exerciseTime)
            }
        }
        setTimerButtonIcon(timer.isRunning)
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