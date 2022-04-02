package com.lukasz.witkowski.training.planner.training.trainingsList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lukasz.witkowski.training.planner.databinding.TrainingListItemBinding
import com.lukasz.witkowski.training.planner.exercise.domain.ExerciseCategory
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlan

class TrainingsAdapter(private val onTrainingClicked: (String, String) -> Unit) :
    ListAdapter<TrainingPlan, TrainingsAdapter.TrainingsViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrainingsViewHolder {
        val binding = TrainingListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TrainingsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrainingsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TrainingsViewHolder(private val binding: TrainingListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TrainingPlan) {
            binding.trainingNameTv.text = item.title
            val categories = item.exercises.filter {
                it.category != ExerciseCategory.NONE
            }.map { it.category }
            setUpCategoriesRecyclerView(emptyList())
            binding.root.setOnClickListener {
                onTrainingClicked(item.id.value, item.title)
            }
        }

        private fun setUpCategoriesRecyclerView(categories: List<String>) {
            binding.categoriesRv.adapter = CategoriesAdapter(categories)
            binding.categoriesRv.layoutManager =
                LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<TrainingPlan>() {
        override fun areItemsTheSame(
            oldItem: TrainingPlan,
            newItem: TrainingPlan
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: TrainingPlan,
            newItem: TrainingPlan
        ): Boolean {
            return oldItem == newItem
        }
    }
}
