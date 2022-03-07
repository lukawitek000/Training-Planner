package com.lukasz.witkowski.training.planner.ui.currentTraining

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.lukasz.witkowski.shared.trainingControllers.CurrentTrainingState
import com.lukasz.witkowski.shared.trainingControllers.TimerHelper
import com.lukasz.witkowski.shared.utils.TimeFormatter
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.databinding.FragmentTrainingExerciseBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class TrainingExerciseFragment : Fragment() {

    private lateinit var binding: FragmentTrainingExerciseBinding

    private lateinit var trainingService: WearableTrainingService

    private lateinit var timer: TimerHelper

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            trainingService = (service as WearableTrainingService.LocalBinder).getService()
            timer = trainingService.timerHelper
            observeCurrentExercise()
            setPlayButtonListener()
            setNextExerciseButtonListener()
            observeExerciseTimer()
        }

        override fun onServiceDisconnected(name: ComponentName?) = Unit
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrainingExerciseBinding.inflate(inflater, container, false)
        val serviceIntent = Intent(requireContext(), WearableTrainingService::class.java)
        requireActivity().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unbindService(connection)
    }

    private fun observeExerciseTimer() {
        timer.timeLeft.observe(viewLifecycleOwner) {
//            if (trainingService.trainingProgressController.isExerciseState && timer.isRunning) {
//                binding.timerTv.text = TimeFormatter.millisToTimer(it)
//            }
        }
        timer.timerFinished.observe(viewLifecycleOwner) {
            if (it) {
                setTimerButtonIcon(isTimerRunning = false)
            }
        }
    }

    private fun setNextExerciseButtonListener() {
        binding.nextBtn.setOnClickListener {
            timer.cancelTimer()
            navigateFurther()
        }
    }

    private fun navigateFurther() {
//        if(trainingService.trainingProgressController.isRestTimeNext() || trainingService.trainingProgressController.isLastExercise()){
//            trainingService.trainingProgressController.navigateToTrainingRestTime()
//        } else {
//            animateView()
//        }
    }

    private fun animateView() {
        val v = binding.constraintLayout
        v.animate().alpha(0f).setListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    Timber.d("Animation enddddd")
//                    trainingService.trainingProgressController.navigateToTrainingRestTime()
                    animateBack(v)
                }
            }
        )
    }

    private fun animateBack(it: View) {
        it.animate().alpha(1f).setListener(
            object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    Timber.d("Animation enddddd coming back")
                }
            }
        )
    }

    private fun setPlayButtonListener() {
        binding.startPauseTimerBtn.setOnClickListener {
            if (!timer.isRunning && !timer.isPaused) {
//                timer.startTimer(trainingService.trainingProgressController.exerciseTime)
            } else if (timer.isPaused) {
                timer.resumeTimer()
            } else if (timer.isRunning) {
                timer.pauseTimer()
            }
            setTimerButtonIcon(timer.isRunning)
        }
    }

    private fun observeCurrentExercise() {
        Timber.d("Observe current exercise")
//        trainingService.trainingProgressController.currentTrainingState.observe(
//            viewLifecycleOwner
//        ) {
//            Timber.d("Observe current exercise state $it")
//            if (it is CurrentTrainingState.ExerciseState) {
//                setExerciseDataToUi(
//                    it.exercise,
//                    trainingService.trainingProgressController.exerciseTime
//                )
//            }
//        }
    }

//    private fun setExerciseDataToUi(trainingExercise: TrainingExercise, exerciseTime: Long) {
//        binding.exerciseNameTv.text = trainingExercise.name
//        binding.repetitionsTv.text = getString(R.string.reps_text, trainingExercise.repetitions)
//        binding.setsTv.text = getString(R.string.sets_text, trainingExercise.sets)
//        binding.apply {
//            if (exerciseTime == 0L) {
//                timerTv.visibility = View.GONE
//                startPauseTimerBtn.visibility = View.GONE
//            } else {
//                timerTv.visibility = View.VISIBLE
//                startPauseTimerBtn.visibility = View.VISIBLE
//                timerTv.text = TimeFormatter.millisToTimer(exerciseTime)
//            }
//        }
//        setTimerButtonIcon(timer.isRunning)
//    }

    private fun setTimerButtonIcon(isTimerRunning: Boolean = false) {
        val icon = if (isTimerRunning) R.drawable.ic_pause else R.drawable.ic_play_arrow
        binding.startPauseTimerBtn.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                icon
            )
        )
    }
}