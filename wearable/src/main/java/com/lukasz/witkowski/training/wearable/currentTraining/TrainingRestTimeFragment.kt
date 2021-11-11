package com.lukasz.witkowski.training.wearable.currentTraining

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.lukasz.witkowski.shared.utils.TimeFormatter
import com.lukasz.witkowski.training.wearable.R
import com.lukasz.witkowski.training.wearable.databinding.FragmentTrainingRestTimeBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class TrainingRestTimeFragment : Fragment() {

    private lateinit var binding: FragmentTrainingRestTimeBinding
    private val viewModel: CurrentTrainingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrainingRestTimeBinding.inflate(layoutInflater, container, false)

        observeSkipRestTimeButton()
        setRestTimer()
        //viewModel.startRestTimer()
        Timber.d("on Create trauubg rest tune")

        // TODO remove it once everything is working
        binding.restTimeTv.setOnClickListener {
            viewModel.navigateToTrainingSummary()
        }

        return binding.root
    }


    private fun setRestTimer() {
        viewModel.currentRestTime.observe(viewLifecycleOwner) {
            Timber.d("Rest time $it")
            binding.restTimeTimerTv.text = TimeFormatter.millisToTimer(it)
        }

    }

    private fun observeSkipRestTimeButton() {
        binding.skipRestTimeTv.setOnClickListener {
            viewModel.navigateToTrainingExercise()
        }
    }

}