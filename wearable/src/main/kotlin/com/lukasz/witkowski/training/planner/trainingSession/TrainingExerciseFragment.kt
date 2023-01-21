package com.lukasz.witkowski.training.planner.trainingSession

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.lukasz.witkowski.shared.time.Time
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.databinding.FragmentTrainingExerciseBinding
import com.lukasz.witkowski.training.planner.training.presentation.models.TrainingExercise
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrainingExerciseFragment : Fragment() {

    private lateinit var binding: FragmentTrainingExerciseBinding
    private val sharedViewModel by activityViewModels<TrainingSessionViewModel>()
    private val viewModel by viewModels<TrainingExerciseViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrainingExerciseBinding.inflate(inflater, container, false)
        viewModel.setCurrentExercise(sharedViewModel.trainingExercise())
        setUpViews()
        return binding.root
    }

    private fun setUpViews() {
        val trainingExercise = viewModel.currentExercise
        binding.apply {
            exerciseNameTv.text = trainingExercise.exercise.name
            repetitionsTv.text = getString(R.string.reps_text, trainingExercise.repetitions)
            setsTv.text = getString(R.string.sets_text, trainingExercise.sets)
        }
        setUpTimerView(trainingExercise)
    }

    private fun setUpTimerView(trainingExercise: TrainingExercise) {
        binding.apply {
            val time = trainingExercise.time
            if(time.isNotZero()) {
                timerLayout.visibility = View.VISIBLE
                timerTv.text = time.toTimerString(false)
            } else {
                timerLayout.visibility = View.GONE
            }
        }
    }

    companion object {
        fun newInstance(): TrainingExerciseFragment {
            return TrainingExerciseFragment()
        }
    }
}
