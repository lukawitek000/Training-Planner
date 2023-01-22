package com.lukasz.witkowski.training.planner.trainingSession

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.lukasz.witkowski.shared.time.Time
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.databinding.FragmentTrainingExerciseBinding
import com.lukasz.witkowski.training.planner.training.presentation.models.TrainingExercise
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class TrainingExerciseFragment : Fragment() {

    private lateinit var binding: FragmentTrainingExerciseBinding
    private val sharedViewModel by activityViewModels<TrainingSessionViewModel>()
    private val viewModel by viewModels<TimerViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrainingExerciseBinding.inflate(inflater, container, false)
        observeTrainingExercise()
        setUpButtonsListeners()
        return binding.root
    }


    private fun observeTrainingExercise() {
        sharedViewModel.currentExercise.observe(viewLifecycleOwner) {
            populateUi(it)
            setUpTimer(it)
        }
    }

    private fun populateUi(trainingExercise: TrainingExercise) {
        binding.apply {
            exerciseNameTv.text = trainingExercise.exercise.name
            repetitionsTv.text = getString(R.string.reps_text, trainingExercise.repetitions)
            setsTv.text = getString(R.string.sets_text, trainingExercise.sets)
        }
        setUpTimerView(trainingExercise)
    }

    private fun setUpTimerView(trainingExercise: TrainingExercise) {
        binding.apply {
            val time = trainingExercise.time
            if (time.isNotZero()) {
                timerLayout.visibility = View.VISIBLE
                setTimeOnTimer(time)
            } else {
                timerLayout.visibility = View.GONE
            }
        }
        setUpTimerIcon()
    }

    private fun setUpTimerIcon() {
        launchInStartedState {
            viewModel.isRunning.collectLatest {
                val icon = if (it) R.drawable.ic_pause else R.drawable.ic_play_arrow
                binding.startPauseTimerBtn.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        icon
                    )
                )
            }
        }
    }

    private fun setUpButtonsListeners() {
        setUpCompletedButton()
        setUpSkipButton()
        setUpTimerControlButton()
    }

    private fun setUpCompletedButton() {
        binding.completedBtn.setOnClickListener {
            stopTimer()
            sharedViewModel.completed()
        }
    }

    private fun setUpSkipButton() {
        binding.skipBtn.setOnClickListener {
            stopTimer()
            sharedViewModel.skip()
        }
    }

    private fun stopTimer() = viewModel.stopTimer()

    private fun setUpTimerControlButton() {
        binding.startPauseTimerBtn.setOnClickListener {
            if (viewModel.isRunning.value) {
                viewModel.pauseTimer()
            } else {
                viewModel.startTimer()
            }
        }
    }

    private fun setUpTimer(trainingExercise: TrainingExercise) {
        if (trainingExercise.time.isZero()) return
        observeTimer()
        viewModel.setTimer(trainingExercise.time)
    }

    private fun observeTimer() {
        launchInStartedState {
            viewModel.timer.collectLatest {
                setTimeOnTimer(it)
            }
        }
    }

    private fun setTimeOnTimer(time: Time) {
        binding.timerTv.text = time.toTimerString(false)
    }

    companion object {
        fun newInstance(): TrainingExerciseFragment {
            return TrainingExerciseFragment()
        }
    }
}
