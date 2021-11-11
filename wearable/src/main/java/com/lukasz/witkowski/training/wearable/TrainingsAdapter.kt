package com.lukasz.witkowski.training.wearable

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lukasz.witkowski.shared.models.Category
import com.lukasz.witkowski.shared.models.TrainingWithExercises
import com.lukasz.witkowski.training.wearable.databinding.TrainingListItemBinding

class TrainingsAdapter(private val onTrainingClicked: (Long, String) -> Unit) :
    ListAdapter<TrainingWithExercises, TrainingsAdapter.TrainingsViewHolder>(DiffCallback()) {

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
        fun bind(item: TrainingWithExercises) {
            binding.trainingNameTv.text = item.training.title
            val categories = item.exercises.map { it.exercise.category }
            setUpCategoriesRecyclerView(categories)
            binding.root.setOnClickListener {
                onTrainingClicked(item.training.id, item.training.title)
            }
        }

        private fun setUpCategoriesRecyclerView(categories: List<Category>) {
            binding.categoriesRv.adapter = CategoriesAdapter(categories)
            binding.categoriesRv.layoutManager =
                LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<TrainingWithExercises>() {
        override fun areItemsTheSame(
            oldItem: TrainingWithExercises,
            newItem: TrainingWithExercises
        ): Boolean {
            return oldItem.training.id == newItem.training.id
        }

        override fun areContentsTheSame(
            oldItem: TrainingWithExercises,
            newItem: TrainingWithExercises
        ): Boolean {
            return oldItem == newItem
        }
    }
}
