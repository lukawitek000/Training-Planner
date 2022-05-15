package com.lukasz.witkowski.training.planner.traininglist

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lukasz.witkowski.training.planner.databinding.TrainingListItemBinding
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import com.lukasz.witkowski.training.planner.training.presentation.models.TrainingPlan

class TrainingPlansAdapter(
    private val context: Context,
    private val onTrainingClicked: (TrainingPlanId, String) -> Unit
) : ListAdapter<TrainingPlan, TrainingPlansAdapter.TrainingsViewHolder>(DiffCallback()) {

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

        fun bind(trainingPlan: TrainingPlan) {
            binding.trainingNameTv.text = trainingPlan.title
            setUpCategoriesRecyclerView(trainingPlan)
            setTrainingPlanClickListener(trainingPlan)
        }

        private fun setTrainingPlanClickListener(item: TrainingPlan) {
            binding.root.setOnClickListener {
                onTrainingClicked(item.id, item.title)
            }
        }

        private fun setUpCategoriesRecyclerView(trainingPlan: TrainingPlan) {
            val categories = trainingPlan.getCategories()
            binding.categoriesRv.adapter = CategoriesAdapter(categories, context)
            binding.categoriesRv.layoutManager =
                LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<TrainingPlan>() {
        override fun areItemsTheSame(oldItem: TrainingPlan, newItem: TrainingPlan) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: TrainingPlan, newItem: TrainingPlan) =
            oldItem == newItem
    }
}
