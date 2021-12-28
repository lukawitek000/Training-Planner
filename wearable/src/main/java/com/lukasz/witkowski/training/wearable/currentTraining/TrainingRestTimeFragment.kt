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
import com.lukasz.witkowski.training.wearable.currentTraining.service.TrainingService
import com.lukasz.witkowski.training.wearable.databinding.FragmentTrainingRestTimeBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class TrainingRestTimeFragment : Fragment() {

    private lateinit var binding: FragmentTrainingRestTimeBinding
//    private val viewModel: CurrentTrainingViewModel by activityViewModels()
    private val timerViewModel: TimerViewModel by viewModels()

    private lateinit var trainingService: TrainingService

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            trainingService = (service as TrainingService.LocalBinder).getService()
            observeRestTimer()
            observeState()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Timber.d("Service disconnected")
        }

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
//        observeRestTimer()
//        observeState()

        val serviceIntent = Intent(requireContext(), TrainingService::class.java)
        requireActivity().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)

        // TODO remove it once everything is working
//        binding.restTimeTv.setOnClickListener {
////            viewModel.navigateToTrainingSummary()
//            trainingService.currentTrainingProgressHelper.navigateToTrainingSummary()
//        }

        return binding.root
    }

    private fun observeState() {
//        viewModel.currentTrainingState.observe(viewLifecycleOwner) {
//            if (it is CurrentTrainingState.RestTimeState) {
//                timerViewModel.startTimer(viewModel.restTime)
//            }
//        }
        trainingService.currentTrainingProgressHelper.currentTrainingState.observe(viewLifecycleOwner) {
            if (it is CurrentTrainingState.RestTimeState) {
                timerViewModel.startTimer(trainingService.currentTrainingProgressHelper.restTime)
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
//        viewModel.navigateToTrainingExercise()
        trainingService.currentTrainingProgressHelper.navigateToTrainingExercise()
    }
}
