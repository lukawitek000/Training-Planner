package com.lukasz.witkowski.training.planner.trainingSession

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.lukasz.witkowski.training.planner.databinding.FragmentTrainingRestTimeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TrainingRestTimeFragment : Fragment() {

    private lateinit var binding: FragmentTrainingRestTimeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrainingRestTimeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    companion object {
        fun newInstance(): TrainingRestTimeFragment {
            return TrainingRestTimeFragment()
        }
    }
}
