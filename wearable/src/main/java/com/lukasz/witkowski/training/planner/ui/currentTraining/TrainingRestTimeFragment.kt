package com.lukasz.witkowski.training.planner.ui.currentTraining

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
import com.lukasz.witkowski.shared.trainingControllers.TimerHelper
import com.lukasz.witkowski.shared.time.TimeFormatter
import com.lukasz.witkowski.training.planner.databinding.FragmentTrainingRestTimeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrainingRestTimeFragment : Fragment() {

    private lateinit var binding: FragmentTrainingRestTimeBinding

    private lateinit var trainingService: WearableTrainingService
    private lateinit var timer: TimerHelper

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            trainingService = (service as WearableTrainingService.LocalBinder).getService()
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
        val serviceIntent = Intent(requireContext(), WearableTrainingService::class.java)
        requireActivity().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
        return binding.root
    }

    private fun observeState() {
//        trainingService.trainingProgressController.currentTrainingState.observe(
//            viewLifecycleOwner
//        ) {
//            if (it is CurrentTrainingState.RestTimeState) {
//                if (!timer.isRunning && !timer.isPaused) {
//                    timer.startTimer(trainingService.trainingProgressController.restTime)
//                }
//            }
//        }
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
//        trainingService.trainingProgressController.navigateToTrainingExercise()
    }
}
