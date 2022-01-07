package com.lukasz.witkowski.training.planner.service

import androidx.lifecycle.lifecycleScope
import com.lukasz.witkowski.shared.models.statistics.TrainingCompleteStatistics
import com.lukasz.witkowski.shared.services.SendingDataService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SendingStatisticsService : SendingDataService() {

    override fun observeNotSynchronizedData() {
        lifecycleScope.launch {
            syncDataRepository.getNotSynchronizedStatistics().collect {
                if (it.isNotEmpty()) {
                    Timber.d("Send statistics ${it.size} $it")
                    sendStatistics(it)
                }
            }
        }
    }

    private fun sendStatistics(statistics: List<TrainingCompleteStatistics>) {

    }


}