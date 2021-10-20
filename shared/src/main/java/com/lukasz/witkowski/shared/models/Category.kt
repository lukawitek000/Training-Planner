package com.lukasz.witkowski.shared.models

sealed class Category(val name: String) {

    companion object {
        val allCategories = Category::class.sealedSubclasses.map { it.objectInstance as Category }
    }
    object None: Category("")
    object Legs : Category("Legs")
    object Shoulders : Category("Shoulders")


}
