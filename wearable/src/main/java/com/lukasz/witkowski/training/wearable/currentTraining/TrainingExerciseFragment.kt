package com.lukasz.witkowski.training.wearable.currentTraining

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.lukasz.witkowski.training.wearable.R
import com.lukasz.witkowski.training.wearable.databinding.FragmentTrainingExerciseBinding


class TrainingExerciseFragment : Fragment() {

    private lateinit var binding: FragmentTrainingExerciseBinding
    private val viewModel: CurrentTrainingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrainingExerciseBinding.inflate(inflater, container, false)

        binding.nextBtn.setOnClickListener {
            viewModel.navigateToTrainingRestTime()
        }

        return binding.root
    }

}