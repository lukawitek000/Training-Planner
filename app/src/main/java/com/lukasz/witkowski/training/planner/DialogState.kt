package com.lukasz.witkowski.training.planner

import androidx.compose.runtime.Composable

sealed class DialogState<T>(val value: T?) {

    @Composable
    fun IsOpen(content: @Composable (T) -> Unit) {
        value?.let {
            content.invoke(it)
        }
    }

    class Open<T>(val content: T) : DialogState<T>(content)
    class Closed<T> : DialogState<T>(null)
}
