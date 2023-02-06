package com.lukasz.witkowski.training.planner.session.service

import com.lukasz.witkowski.training.planner.statistics.presentation.TimerController

class RestTimeTimer(timerController: TimerController): TimerController by timerController
