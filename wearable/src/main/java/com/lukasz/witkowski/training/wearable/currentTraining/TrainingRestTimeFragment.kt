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
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.lukasz.witkowski.shared.utils.TimeFormatter
import com.lukasz.witkowski.training.wearable.currentTraining.service.TimerHelper
import com.lukasz.witkowski.training.wearable.currentTraining.service.TrainingService
import com.lukasz.witkowski.training.wearable.databinding.FragmentTrainingRestTimeBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class TrainingRestTimeFragment : Fragment() {

    private lateinit var binding: FragmentTrainingRestTimeBinding

    private lateinit var trainingService: TrainingService
    private lateinit var timer: TimerHelper

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            trainingService = (service as TrainingService.LocalBinder).getService()
            timer = trainingService.timerHelper
            observeRestTimer()
            observeState()
        }

        override fun onServiceDisconnected(name: ComponentName?) = Unit
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unbindService(connection)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrainingRestTimeBinding.inflate(layoutInflater, container, false)
        observeSkipRestTimeButton()
        val serviceIntent = Intent(requireContext(), TrainingService::class.java)
        requireActivity().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
        return binding.root
    }

    private fun observeState() {
        trainingService.currentTrainingProgressHelper.currentTrainingState.observe(
            viewLifecycleOwner
        ) {
            if (it is CurrentTrainingState.RestTimeState) {
                if (!timer.isRunning && !timer.isPaused) {
                    timer.startTimer(trainingService.currentTrainingProgressHelper.restTime)
                }
            }
        }
    }

    private fun observeRestTimer() {
        timer.timeLeft.observe(viewLifecycleOwner) {
            binding.restTimeTimerTv.text = TimeFormatter.millisToTimer(it)
        }
    }

    private fun observeSkipRestTimeButton() {
        binding.skipRestTimeTv.setOnClickListener {
            exitRestTimeFragment()
        }
    }

    private fun exitRestTimeFragment() {
        timer.cancelTimer()
        trainingService.currentTrainingProgressHelper.navigateToTrainingExercise()
    }
}
