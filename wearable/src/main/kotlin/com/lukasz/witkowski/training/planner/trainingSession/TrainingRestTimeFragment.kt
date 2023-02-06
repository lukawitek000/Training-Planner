package com.lukasz.witkowski.training.planner.trainingSession

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.lukasz.witkowski.training.planner.WearableTrainingPlannerViewModelFactory
import com.lukasz.witkowski.training.planner.databinding.FragmentTrainingRestTimeBinding
import com.lukasz.witkowski.training.planner.statistics.presentation.TrainingSessionState
import com.lukasz.witkowski.training.planner.utils.launchInStartedState
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

class TrainingRestTimeFragment : Fragment() {

    private lateinit var binding: FragmentTrainingRestTimeBinding
    private val sharedViewModel by activityViewModels<TrainingSessionViewModel>()
    private val viewModel by viewModels<TimerViewModel>(factoryProducer = {
        WearableTrainingPlannerViewModelFactory()
    })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrainingRestTimeBinding.inflate(layoutInflater, container, false)
        setUpSkipButton()
        observeTrainingExercise()
        return binding.root
    }

    private fun setUpSkipButton() {
        binding.skipRestTimeTv.setOnClickListener {
            viewModel.stopTimer()
            skipRestTime()
        }
    }

    private fun observeTrainingExercise() {
        sharedViewModel.trainingSessionState.observe(viewLifecycleOwner) {
            if(it is TrainingSessionState.RestTimeState) {
                observeTimer()
                observeTimerFinished()
                viewModel.setTimer(it.time)
                viewModel.startTimer()
            }
        }
    }

    private fun observeTimer() = launchInStartedState {
        viewModel.timer.collectLatest {
            binding.restTimeTimerTv.text = it.toTimerString(false)
        }
    }

    private fun observeTimerFinished() = launchInStartedState {
        viewModel.hasFinished.collectLatest {
            if (it) {
                skipRestTime()
            }
        }
    }

    private fun skipRestTime() = sharedViewModel.skip()

    companion object {
        fun newInstance(): TrainingRestTimeFragment {
            return TrainingRestTimeFragment()
        }
    }
}
