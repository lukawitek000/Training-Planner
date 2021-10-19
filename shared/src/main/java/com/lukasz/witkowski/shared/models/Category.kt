package com.lukasz.witkowski.shared.models

sealed class Category(val name: String) {

    object Legs : Category("Legs")
    object Shoulders : Category("Shoulders")


}
