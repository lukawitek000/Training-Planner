package com.lukasz.witkowski.shared.models

sealed class Category(val name: String) {

    object None: Category("")
    object Legs : Category("Legs")
    object Shoulders : Category("Shoulders")
}
