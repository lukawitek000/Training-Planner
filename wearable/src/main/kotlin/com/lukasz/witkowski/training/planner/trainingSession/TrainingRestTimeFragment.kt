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
import com.lukasz.witkowski.training.planner.session.service.SessionServiceConnector
import com.lukasz.witkowski.training.planner.statistics.presentation.TimerController
import com.lukasz.witkowski.training.planner.statistics.presentation.TrainingSessionState
import com.lukasz.witkowski.training.planner.utils.launchInStartedState
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

class TrainingRestTimeFragment : Fragment() {

    private lateinit var binding: FragmentTrainingRestTimeBinding
    private val sharedViewModel by activityViewModels<TrainingSessionViewModel>()
    private val serviceConnector = SessionServiceConnector()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrainingRestTimeBinding.inflate(layoutInflater, container, false)
        serviceConnector.setTimerReadyCallback {
            setUpSkipButton(it)
            observeTimer(it)
        }
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

    private fun setUpSkipButton(timerController: TimerController) {
        binding.skipRestTimeTv.setOnClickListener {
            timerController.stopTimer()
            sharedViewModel.skip()
        }
    }

    private fun observeTimer(timerController: TimerController) = launchInStartedState {
        timerController.timer.collectLatest {
            binding.restTimeTimerTv.text = it.toTimerString(false)
        }
    }

    companion object {
        fun newInstance(): TrainingRestTimeFragment {
            return TrainingRestTimeFragment()
        }
    }
}
