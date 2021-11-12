package com.lukasz.witkowski.training.wearable.currentTraining

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.lukasz.witkowski.shared.models.TrainingExercise
import com.lukasz.witkowski.shared.utils.TimeFormatter
import com.lukasz.witkowski.training.wearable.R
import com.lukasz.witkowski.training.wearable.databinding.FragmentTrainingExerciseBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class TrainingExerciseFragment : Fragment() {

    private lateinit var binding: FragmentTrainingExerciseBinding
    private val viewModel: CurrentTrainingViewModel by activityViewModels()
    private val timerViewModel: TimerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrainingExerciseBinding.inflate(inflater, container, false)
        observeCurrentExercise()
        setPlayButtonListener()
        setNextExerciseButtonListener()
        observeExerciseTimer()

        return binding.root
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
            viewModel.navigateToTrainingRestTime()
            timerViewModel.cancelTimer()
        }
    }

    private fun setPlayButtonListener() {
        binding.startPauseTimerBtn.setOnClickListener {
            if (!timerViewModel.isRunning && !timerViewModel.isPaused) {
                timerViewModel.startTimer(viewModel.exerciseTime)
            } else if (timerViewModel.isPaused) {
                timerViewModel.resumeTimer()
            } else if (timerViewModel.isRunning) {
                timerViewModel.pauseTimer()
            }
            setTimerButtonIcon(timerViewModel.isRunning)
        }
    }

    private fun observeCurrentExercise() {
        viewModel.currentExercise.observe(viewLifecycleOwner) {
            setExerciseDataToUi(it)
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