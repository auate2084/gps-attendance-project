package com.gpsattendance.mobile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gpsattendance.mobile.data.model.WorkSessionResponse
import com.gpsattendance.mobile.data.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun refreshSessions() {
        refreshVisibleSessions()
    }

    fun refreshVisibleSessions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            attendanceRepository.visibleSessions()
                .onSuccess { sessions ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        sessions = sessions,
                        memberPins = buildMemberPins(sessions)
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message ?: "Failed to load team sessions"
                    )
                }
        }
    }

    fun updateLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                myLatitude = latitude,
                myLongitude = longitude
            )
            attendanceRepository.updateMyLocation(latitude, longitude)
                .onSuccess { result ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        trackingMessage = "${result.state}: ${result.message}"
                    )
                    refreshVisibleSessions()
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = it.message ?: "Failed to update location"
                    )
                }
        }
    }

    fun setMyLocation(latitude: Double, longitude: Double) {
        _uiState.value = _uiState.value.copy(myLatitude = latitude, myLongitude = longitude)
    }

    private fun buildMemberPins(sessions: List<WorkSessionResponse>): List<TeamMemberPin> {
        return sessions
            .asSequence()
            .filter { it.lastLatitude != null && it.lastLongitude != null }
            .groupBy { it.userId }
            .mapNotNull { (_, userSessions) ->
                val latest = userSessions.maxByOrNull { it.checkInAt ?: "" } ?: return@mapNotNull null
                val lat = latest.lastLatitude ?: return@mapNotNull null
                val lng = latest.lastLongitude ?: return@mapNotNull null
                TeamMemberPin(
                    userId = latest.userId,
                    userName = latest.userName,
                    status = latest.status,
                    latitude = lat,
                    longitude = lng
                )
            }
            .toList()
    }
}

data class TeamMemberPin(
    val userId: Long,
    val userName: String,
    val status: String,
    val latitude: Double,
    val longitude: Double
)

data class MainUiState(
    val isLoading: Boolean = false,
    val sessions: List<WorkSessionResponse> = emptyList(),
    val memberPins: List<TeamMemberPin> = emptyList(),
    val myLatitude: Double? = null,
    val myLongitude: Double? = null,
    val trackingMessage: String? = null,
    val error: String? = null
)
