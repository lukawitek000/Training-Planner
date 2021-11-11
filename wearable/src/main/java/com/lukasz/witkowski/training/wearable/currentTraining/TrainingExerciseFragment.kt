package com.lukasz.witkowski.training.wearable.currentTraining

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import com.lukasz.witkowski.shared.models.TrainingExercise
import com.lukasz.witkowski.shared.utils.TimeFormatter
import com.lukasz.witkowski.training.wearable.R
import com.lukasz.witkowski.training.wearable.databinding.FragmentTrainingExerciseBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class TrainingExerciseFragment : Fragment() {

    private lateinit var binding: FragmentTrainingExerciseBinding
    private val viewModel: CurrentTrainingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrainingExerciseBinding.inflate(inflater, container, false)
        observeCurrentExercise()
        setPlayButtonListener()
        setNextExerciseButtonListener()
        Timber.d("on Create trauubg exercise")

        return binding.root
    }

    private fun setNextExerciseButtonListener() {
        binding.nextBtn.setOnClickListener {
            viewModel.navigateToTrainingRestTime()
        }
    }

    private fun setPlayButtonListener() {
        binding.startPauseTimerBtn.apply {
            setOnClickListener {
                viewModel.isExerciseTimerRunning = !viewModel.isExerciseTimerRunning
                setTimerButtonIcon(viewModel.isExerciseTimerRunning)
            }
        }
    }

    private fun observeCurrentExercise() {
        viewModel.currentExercise.observe(viewLifecycleOwner) {
            setExerciseDataToUi(it)
        }
    }

    private fun setExerciseDataToUi(trainingExercise: TrainingExercise) {
        binding.exerciseNameTv.text = trainingExercise.exercise.name
        binding.repetitionsTv.text = getString(R.string.reps_text, trainingExercise.repetitions)
        binding.setsTv.text = getString(R.string.sets_text, trainingExercise.sets)
        val time = trainingExercise.time
        binding.apply {
            if(time == 0L){
                timerTv.visibility = View.GONE
                startPauseTimerBtn.visibility = View.GONE
            } else {
                timerTv.visibility = View.VISIBLE
                startPauseTimerBtn.visibility = View.VISIBLE
                timerTv.text = TimeFormatter.millisToTimer(time)
            }
        }
        setTimerButtonIcon()
    }

    private fun setTimerButtonIcon(isTimerRunning: Boolean = false) {
        val icon = if(isTimerRunning) R.drawable.ic_play_arrow else R.drawable.ic_pause
        binding.startPauseTimerBtn.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                icon
            )
        )
    }
}