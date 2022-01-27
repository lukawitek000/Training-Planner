package com.lukasz.witkowski.training.planner.service

import androidx.lifecycle.lifecycleScope
import com.lukasz.witkowski.shared.service.SendingDataService
import com.lukasz.witkowski.shared.utils.STATISTICS_PATH
import com.lukasz.witkowski.shared.utils.SYNC_SUCCESSFUL
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class SendingStatisticsService : SendingDataService() {

    override fun observeNotSynchronizedData() {
        lifecycleScope.launch {
            syncDataRepository.getNotSynchronizedStatistics().collect {
                if (it.isNotEmpty()) {
                    Timber.d("Send statistics ${it.size} $it")
                    sendData(data = it, path = STATISTICS_PATH)
                }
            }
        }
    }

    override suspend fun handleSyncResponse(id: Long, syncResponse: Int) {
        Timber.d("Sending statistics $id response $syncResponse")
        if(syncResponse == SYNC_SUCCESSFUL) {
            syncDataRepository.updateSynchronizedStatistics(id)
        } else {
            Timber.w("Sending statistics $id failed")
        }
    }
}
