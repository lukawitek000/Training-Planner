package com.lukasz.witkowski.shared

sealed class Category(val name: String) {

    object Legs : Category("Legs")
    object Shoulders : Category("Shoulders")


}
