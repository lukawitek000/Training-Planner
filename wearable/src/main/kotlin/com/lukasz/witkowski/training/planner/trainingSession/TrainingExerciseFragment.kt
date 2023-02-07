package com.lukasz.witkowski.training.planner.trainingSession

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.lukasz.witkowski.shared.time.Time
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.databinding.FragmentTrainingExerciseBinding
import com.lukasz.witkowski.training.planner.session.service.SessionServiceConnector
import com.lukasz.witkowski.training.planner.statistics.presentation.TimerController
import com.lukasz.witkowski.training.planner.statistics.presentation.TrainingSessionState
import com.lukasz.witkowski.training.planner.training.presentation.models.TrainingExercise
import com.lukasz.witkowski.training.planner.utils.launchInStartedState
import kotlinx.coroutines.flow.collectLatest

class TrainingExerciseFragment : Fragment() {

    private lateinit var binding: FragmentTrainingExerciseBinding
    private val sharedViewModel by activityViewModels<TrainingSessionViewModel>()
    private val serviceConnector = SessionServiceConnector()
    private lateinit var timerController: TimerController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrainingExerciseBinding.inflate(inflater, container, false)
        observeTrainingExercise()
        serviceConnector.setTimerReadyCallback {
            timerController = it
            setUpButtonsListeners()
            // this methods are needed only if the time is set
            setUpTimerIcon()
            observeTimer()
        }
        serviceConnector.bindService(requireContext())
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        serviceConnector.unbindService(requireContext())
    }

    private fun observeTrainingExercise() {
        sharedViewModel.trainingSessionState.observe(viewLifecycleOwner) {
            if (it is TrainingSessionState.ExerciseState) {
                populateUi(it.currentExercise)
                setUpTimerView(it.currentExercise.time)
            }
        }
    }

    private fun populateUi(trainingExercise: TrainingExercise) {
        binding.apply {
            exerciseNameTv.text = trainingExercise.exercise.name
            repetitionsTv.text = getString(R.string.reps_text, trainingExercise.repetitions)
            setsTv.text = getString(R.string.sets_text, trainingExercise.sets)
        }
    }

    private fun setUpTimerView(time: Time) {
        binding.timerLayout.visibility = if (time.isZero()) View.GONE else View.VISIBLE
    }

    private fun setTimeOnTimer(time: Time) {
        binding.timerTv.text = time.toTimerString(false)
    }

    private fun setUpTimerIcon() = launchInStartedState {
        timerController.isRunning.collectLatest {
            val icon = if (it) R.drawable.ic_pause else R.drawable.ic_play_arrow
            binding.startPauseTimerBtn.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    icon
                )
            )
        }
    }

    private fun observeTimer() = launchInStartedState {
        timerController.timer.collectLatest {
            setTimeOnTimer(it)
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

    private fun stopTimer() = timerController.stopTimer()

    private fun setUpTimerControlButton() {
        binding.startPauseTimerBtn.setOnClickListener {
            // TODO consider adding some toggle method to timer class
            if (timerController.isRunning.value) {
                timerController.pauseTimer()
            } else {
                timerController.resumeTimer()
            }
        }
    }

    companion object {
        fun newInstance(): TrainingExerciseFragment {
            return TrainingExerciseFragment()
        }
    }
}
