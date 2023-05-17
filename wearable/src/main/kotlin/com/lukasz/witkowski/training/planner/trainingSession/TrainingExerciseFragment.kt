package com.lukasz.witkowski.training.planner.trainingSession

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.databinding.FragmentTrainingExerciseBinding
import com.lukasz.witkowski.training.planner.session.service.SessionServiceConnector
import com.lukasz.witkowski.training.planner.shared.time.Time
import com.lukasz.witkowski.training.planner.shared.time.TimeFormatter
import com.lukasz.witkowski.training.planner.statistics.presentation.TrainingSessionState
import com.lukasz.witkowski.training.planner.training.presentation.models.TrainingExercise
import com.lukasz.witkowski.training.planner.utils.launchInStartedState
import kotlinx.coroutines.flow.collectLatest

class TrainingExerciseFragment : Fragment() {

    private lateinit var binding: FragmentTrainingExerciseBinding
    private val sharedViewModel by activityViewModels<TrainingSessionViewModel>()
    private val serviceConnector = SessionServiceConnector()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrainingExerciseBinding.inflate(inflater, container, false)
        observeTrainingExercise()
        setUpButtonsListeners()
        return binding.root
    }

    override fun onStart() {
        serviceConnector.bindService(requireContext())
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        serviceConnector.unbindService(requireContext())
    }

    private fun observeTrainingExercise() {
        sharedViewModel.trainingSessionState.observe(viewLifecycleOwner) {
            if (it is TrainingSessionState.ExerciseState) {
                populateUi(it.currentExercise)
                setUpTimer(it.currentExercise.time)
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

    private fun setUpTimer(time: Time) {
        if (time.isZero()) {
            binding.timerLayout.visibility = View.GONE
            return
        }
        setUpTimerIcon()
        observeTimer()
        observeTimerFinished()
        binding.timerLayout.visibility = View.VISIBLE
    }

    private fun observeTimerFinished() {

    }

    private fun setTimeOnTimer(time: Time) {
        binding.timerTv.text = TimeFormatter(requireContext()).formatTimer(time, false)
    }

    private fun setUpTimerIcon() = launchInStartedState {
        sharedViewModel.isTimerRunning.collectLatest {
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
        sharedViewModel.time.collectLatest {
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

    private fun stopTimer() = sharedViewModel.stopTimer()

    private fun setUpTimerControlButton() {
        binding.startPauseTimerBtn.setOnClickListener {
            sharedViewModel.timerPauseOrResume()
        }
    }

    companion object {
        fun newInstance(): TrainingExerciseFragment {
            return TrainingExerciseFragment()
        }
    }
}
