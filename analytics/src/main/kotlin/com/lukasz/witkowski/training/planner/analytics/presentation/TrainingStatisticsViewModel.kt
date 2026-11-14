package com.lukasz.witkowski.training.planner.analytics.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.lukasz.witkowski.training.planner.analytics.application.TrainingStatisticsService
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatistics
import com.lukasz.witkowski.training.planner.session.domain.models.TrainingStatisticsId
import com.lukasz.witkowski.training.planner.training.domain.TrainingPlanId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class TrainingStatisticsViewModel(
    private val trainingPlanId: TrainingPlanId,
    private val service: TrainingStatisticsService,
) : ViewModel() {

    val statisticsOverviews = service.getOverviews(trainingPlanId).cachedIn(viewModelScope)
    private val selectedSession = MutableStateFlow<TrainingStatisticsId?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    internal val statisticsDetails = selectedSession.flatMapLatest { id ->
        if (id == null) {
            flowOf(DetailsUiState.NoData)
        } else {
            service.getStatistics(id)
                .map { statistics -> statistics?.let { DetailsUiState.Success(it) } ?: DetailsUiState.NoData }
                .catch { emit(DetailsUiState.Failure) }
        }
    }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DetailsUiState.NoData
    )

    fun onSessionClicked(id: TrainingStatisticsId) {
        selectedSession.value = id
    }

    fun onSessionDismissed() {
        selectedSession.value = null
    }
}

internal sealed interface DetailsUiState {
    data class Success(val details: TrainingStatistics) : DetailsUiState
    data object Failure : DetailsUiState
    data object NoData: DetailsUiState
}