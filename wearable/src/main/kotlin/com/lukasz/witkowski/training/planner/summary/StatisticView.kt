package com.lukasz.witkowski.training.planner.summary

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.lukasz.witkowski.training.planner.R
import com.lukasz.witkowski.training.planner.databinding.StatisticValueViewBinding

class StatisticView(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private var binding: StatisticValueViewBinding = StatisticValueViewBinding.inflate(LayoutInflater.from(context), this, true)

    init {
//        binding = StatisticValueViewBinding.inflate(LayoutInflater.from(context), this, true)
        obtainAttributes(context, attrs)
    }

    private fun obtainAttributes(context: Context, attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.StatisticView)
        try {
            binding.statisticIv.apply {
                contentDescription =
                    typedArray.getString(R.styleable.StatisticView_statisticIconContentDescription)
                val icon = typedArray.getDrawable(R.styleable.StatisticView_statisticIcon)
                setImageDrawable(icon)
            }
        } finally {
            typedArray.recycle()
        }
    }

    fun setStatisticValue(value: String) {
        binding.statisticTv.text = value
    }
}
