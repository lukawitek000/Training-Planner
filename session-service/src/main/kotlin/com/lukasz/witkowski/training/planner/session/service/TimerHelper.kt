package com.lukasz.witkowski.training.planner.session.service

import com.lukasz.witkowski.training.planner.statistics.presentation.TimerController
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerHelper(timerController: TimerController): TimerController by timerController {

    private val coroutineScope = CoroutineScope(Dispatchers.Default + CoroutineName("TimerHelper"))

    fun observeHasFinished(action: () -> Unit) {
        coroutineScope.launch {
            hasFinished.collectLatest {
                if (it && isActive) {
                    action()
                }
            }
        }
    }

    fun cancel() {
        coroutineScope.cancel()
    }
}
