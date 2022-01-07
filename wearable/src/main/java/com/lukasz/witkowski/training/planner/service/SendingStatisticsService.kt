package com.lukasz.witkowski.training.planner.service

import androidx.lifecycle.lifecycleScope
import com.lukasz.witkowski.shared.services.SendingDataService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SendingStatisticsService : SendingDataService() {


    override fun onCreate() {
        super.onCreate()

    }

    override fun observeNotSynchronizedData() {
//        TODO("Not yet implemented")
    }


}