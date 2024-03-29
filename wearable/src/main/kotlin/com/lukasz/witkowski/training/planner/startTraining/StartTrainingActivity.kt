package com.lukasz.witkowski.training.planner.startTraining

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.databinding.ActivityStartTrainingBinding
import com.lukasz.witkowski.training.planner.trainingSession.TrainingSessionActivity

class StartTrainingActivity : ComponentActivity() {

    private lateinit var binding: ActivityStartTrainingBinding
    private val viewModel by viewModels<StartTrainingViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TrainingPlannerTheme)
        super.onCreate(savedInstanceState)
        binding = ActivityStartTrainingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setQuestionText()
        setButtonsClickListeners()
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
        val intent = Intent(this, TrainingSessionActivity::class.java)
        intent.putExtra(TRAINING_ID_KEY, viewModel.trainingId)
        startActivity(intent)
    }

    companion object {
        const val TRAINING_ID_KEY = "trainingId"
        const val TRAINING_TITLE_KEY = "trainingTitle"
    }
}
