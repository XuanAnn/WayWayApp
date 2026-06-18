package com.example.waywayapp.presentation.user.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waywayapp.R
import com.example.waywayapp.core.network.RetrofitProvider
import com.example.waywayapp.data.remote.dto.geocoding.GeocodingResponseDto
import com.example.waywayapp.data.repository.HomeRepositoryImpl
import com.example.waywayapp.data.repository.ProfileRepository
import com.example.waywayapp.domain.model.Service
import com.example.waywayapp.presentation.user.home.model.ServiceUI
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.Normalizer

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeState())
    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()

    private val geocodingApi = RetrofitProvider.geocodingApi
    private val homeRepository = HomeRepositoryImpl()
    private val profileRepository = ProfileRepository()
    private var searchJob: Job? = null

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }


            val domainServices = homeRepository.loadServices()
            val uiServices = domainServices.map { it.toServiceUI() }

            _uiState.update {
                it.copy(
                    services = uiServices,
                    isLoading = false,
                    errorMessage = null
                )
            }
        }
    }

    fun onSearchChange(value: String) {
        _uiState.update { it.copy(searchText = value) }

        searchJob?.cancel()
        if (value.trim().length <= 2) {
            _uiState.update { it.copy(searchSuggestions = emptyList()) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(350)
            runCatching {
                searchAddressWithFallback(value.trim())
            }.onSuccess { results ->
                _uiState.update { it.copy(searchSuggestions = results.take(6)) }
            }.onFailure {
                _uiState.update { it.copy(searchSuggestions = emptyList()) }
            }
        }
    }

    fun clearSearchSuggestions() {
        searchJob?.cancel()
        _uiState.update { it.copy(searchSuggestions = emptyList()) }
    }

    fun refreshMomoStatus() {
        viewModelScope.launch {
            runCatching {
                profileRepository.loadUserProfile()
            }.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        momoLinked = user.momoLinked,
                        momoPhone = user.momoPhone,
                        momoDisplayName = user.name.ifBlank { user.email.ifBlank { user.phone } }
                    )
                }
            }
        }
    }

    private suspend fun searchAddressWithFallback(query: String): List<GeocodingResponseDto> {
        val results = geocodingApi.searchAddress(query = query)
        if (results.isNotEmpty()) return results

        val fallbackQuery = query.withoutVietnameseDiacritics()
        if (fallbackQuery == query) return results

        return geocodingApi.searchAddress(query = fallbackQuery)
    }

    private fun String.withoutVietnameseDiacritics(): String {
        return Normalizer.normalize(this, Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
            .replace('\u0111', 'd')
            .replace('\u0110', 'D')
    }
    fun Service.toServiceUI(): ServiceUI {
        return ServiceUI(
            type = type,
            title = title,
            iconRes = getIconResource(type)
        )
    }
    private fun getIconResource(type: String): Int {
        return when (type) {
            "bike" -> R.drawable.bike_icon
            "car" -> R.drawable.car_icon
            "express" -> R.drawable.express_icon
            else -> R.drawable.bike_icon
        }
    }
}
