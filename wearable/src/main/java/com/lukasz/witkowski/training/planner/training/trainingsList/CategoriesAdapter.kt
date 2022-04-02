package com.lukasz.witkowski.training.planner.training.trainingsList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lukasz.witkowski.training.planner.databinding.CategoryListItemBinding

class CategoriesAdapter(private val categories: List<String>) :
    RecyclerView.Adapter<CategoriesAdapter.CategoriesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesViewHolder {
        val binding = CategoryListItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CategoriesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoriesViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    inner class CategoriesViewHolder(private val binding: CategoryListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(category: String) {
            binding.categoryNameTv.text = category
        }

    }
}