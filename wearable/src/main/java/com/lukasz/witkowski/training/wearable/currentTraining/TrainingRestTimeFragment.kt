package com.lukasz.witkowski.training.wearable.currentTraining

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.lukasz.witkowski.training.wearable.R

class TrainingRestTimeFragment : Fragment() {
    private val viewModel: CurrentTrainingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_training_rest_time, container, false)
    }

}