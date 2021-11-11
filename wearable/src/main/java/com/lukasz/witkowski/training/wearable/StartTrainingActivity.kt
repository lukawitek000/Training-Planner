package com.lukasz.witkowski.training.wearable

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.lukasz.witkowski.training.wearable.databinding.ActivityStartTrainingBinding

class StartTrainingActivity : ComponentActivity() {

    companion object {
        const val TRAINING_ID_KEY = "trainingId"
        const val TRAINING_TITLE_KEY = "trainingTitle"
    }

    private lateinit var binding: ActivityStartTrainingBinding
    private val viewModel by viewModels<StartTrainingViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartTrainingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        retrieveTrainingProperties()
        setQuestionText()
        setButtonsClickListeners()
    }

    private fun retrieveTrainingProperties() {
        val trainingId = intent.extras?.getLong(TRAINING_ID_KEY)
        val trainingTitle = intent.extras?.getString(TRAINING_TITLE_KEY)
        if (trainingId == null || trainingTitle == null) {
            finish()
        }
        viewModel.trainingId = trainingId!!
        viewModel.trainingTitle = trainingTitle!!
    }

    private fun setQuestionText() {
        binding.startTrainingQuestionTv.text =
            getString(R.string.start_training_question, viewModel.trainingTitle)
    }

    private fun setButtonsClickListeners() {
        binding.noStartTrainingBtn.setOnClickListener {
            finish()
        }
        binding.yesStartTrainingBtn.setOnClickListener {
            startTraining()
        }
    }

    private fun startTraining() {
        val intent = Intent(this, TrainingExerciseActivity::class.java)
        intent.putExtra(TRAINING_ID_KEY, viewModel.trainingId)
        startActivity(intent)
    }
}
