package com.lukasz.witkowski.training.planner.trainingSession

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.lukasz.witkowski.training.planner.databinding.FragmentTrainingExerciseBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrainingExerciseFragment : Fragment() {

    private lateinit var binding: FragmentTrainingExerciseBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrainingExerciseBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        fun newInstance(): TrainingExerciseFragment {
            return TrainingExerciseFragment()
        }
    }
}
