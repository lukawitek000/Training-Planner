package com.lukasz.witkowski.training.planner.trainingSession

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.lukasz.witkowski.training.planner.databinding.FragmentTrainingRestTimeBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

@AndroidEntryPoint
class TrainingRestTimeFragment : Fragment() {

    private lateinit var binding: FragmentTrainingRestTimeBinding
    private val sharedViewModel by activityViewModels<TrainingSessionViewModel>()
    private val viewModel by viewModels<TimerViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrainingRestTimeBinding.inflate(layoutInflater, container, false)
        observeTrainingExercise()
        setUpSkipButton()
        return binding.root
    }

    private fun observeTrainingExercise() {
        sharedViewModel.currentExercise.observe(viewLifecycleOwner) {
            viewModel.setTimer(it.restTime)
            viewModel.startTimer()
            setUpTimerView()
        }
    }

    private fun setUpTimerView() {
        launchInStartedState {
            observeTimer()
            observeTimerFinished()
        }
    }

    private suspend fun observeTimer() {
        viewModel.timer.collectLatest {
            Timber.d("LWWW timer ${it.toTimerString()}")
            binding.restTimeTimerTv.text = it.toTimerString(false)
            if (it.isZero()) {
                Timber.d("LWWW timer is 0")
                sharedViewModel.skip()
            }
        }
    }

    private fun setUpSkipButton() {
        binding.skipRestTimeTv.setOnClickListener {
            viewModel.stopTimer()
            sharedViewModel.skip()
        }
    }

    private suspend fun observeTimerFinished() {
        viewModel.hasFinished.collectLatest {
            Timber.d("LWWW has finished $it") // TODO never called
            if (it) {
                sharedViewModel.skip()
            }
        }
    }

    companion object {
        fun newInstance(): TrainingRestTimeFragment {
            return TrainingRestTimeFragment()
        }
    }
}
