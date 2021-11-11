package com.lukasz.witkowski.training.wearable

import android.app.Activity
import android.os.Bundle
import androidx.wear.widget.WearableLinearLayoutManager
import com.lukasz.witkowski.training.wearable.databinding.ActivityMainBinding

class MainActivity : Activity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: TrainingsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpTrainingAdapter()
    }

    private fun setUpTrainingAdapter() {
        adapter = TrainingsAdapter()
        adapter.submitList(trainingsList)
        binding.trainingsWatchRv.apply {
            isEdgeItemsCenteringEnabled = true
            layoutManager = WearableLinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }
    }
}
