package com.lukasz.witkowski.training.planner.shared.utils

import kotlin.math.roundToInt

enum class WeightUnit {
    KG,
    LBS
}

@JvmInline
value class Weight private constructor(val inKilograms: Double) : Comparable<Weight> {
    val inPounds: Double
        get() = inKilograms * KG_TO_LBS

    val isPositive: Boolean
        get() = inKilograms > 0.0

    fun toDouble(unit: WeightUnit): Double = when (unit) {
        WeightUnit.KG -> inKilograms
        WeightUnit.LBS -> inPounds
    }

    fun toInt(unit: WeightUnit): Int = toDouble(unit).roundToInt()

    operator fun plus(other: Weight): Weight = Weight(inKilograms + other.inKilograms)

    operator fun minus(other: Weight): Weight = Weight(inKilograms - other.inKilograms)

    operator fun times(factor: Int): Weight = Weight(inKilograms * factor)

    operator fun times(factor: Double): Weight = Weight(inKilograms * factor)

    override fun compareTo(other: Weight): Int = inKilograms.compareTo(other.inKilograms)

    companion object {
        const val LBS_TO_KG = 0.45359237
        const val KG_TO_LBS = 1.0 / LBS_TO_KG

        val ZERO = Weight(0.0)

        fun ofKilograms(kg: Double): Weight = Weight(kg)

        fun ofPounds(lbs: Double): Weight = Weight(lbs * LBS_TO_KG)

        fun of(value: Double, unit: WeightUnit): Weight = when (unit) {
            WeightUnit.KG -> ofKilograms(value)
            WeightUnit.LBS -> ofPounds(value)
        }
    }
}

val Double.kg: Weight get() = Weight.ofKilograms(this)
val Int.kg: Weight get() = Weight.ofKilograms(this.toDouble())
val Double.lbs: Weight get() = Weight.ofPounds(this)
val Int.lbs: Weight get() = Weight.ofPounds(this.toDouble())
