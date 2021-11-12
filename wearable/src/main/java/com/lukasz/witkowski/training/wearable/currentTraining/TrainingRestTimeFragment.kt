package com.lukasz.witkowski.training.wearable.currentTraining

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.lukasz.witkowski.shared.utils.TimeFormatter
import com.lukasz.witkowski.training.wearable.databinding.FragmentTrainingRestTimeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrainingRestTimeFragment : Fragment() {

    private lateinit var binding: FragmentTrainingRestTimeBinding
    private val viewModel: CurrentTrainingViewModel by activityViewModels()
    private val timerViewModel: TimerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrainingRestTimeBinding.inflate(layoutInflater, container, false)

        observeSkipRestTimeButton()
        observeRestTimer()
        observeState()

        // TODO remove it once everything is working
        binding.restTimeTv.setOnClickListener {
            viewModel.navigateToTrainingSummary()
        }

        return binding.root
    }

    private fun observeState() {
        viewModel.currentTrainingState.observe(viewLifecycleOwner) {
            if (it == CurrentTrainingState.RestTimeState) {
                timerViewModel.startTimer(viewModel.restTime)
            }
        }
    }

    private fun observeRestTimer() {
        timerViewModel.timeLeft.observe(viewLifecycleOwner) {
            binding.restTimeTimerTv.text = TimeFormatter.millisToTimer(it)
        }
        timerViewModel.timerFinished.observe(viewLifecycleOwner) {
            if (it) {
                exitRestTimeFragment()
            }
        }
    }

    private fun observeSkipRestTimeButton() {
        binding.skipRestTimeTv.setOnClickListener {
            exitRestTimeFragment()
        }
    }

    private fun exitRestTimeFragment() {
        timerViewModel.cancelTimer()
        viewModel.navigateToTrainingExercise()
    }
}
