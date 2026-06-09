package com.example.waywayapp.ui.user.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.waywayapp.core.network.RetrofitProvider
import com.example.waywayapp.data.remote.dto.geocoding.GeocodingResponseDto
import com.example.waywayapp.ui.user.home.mock.mockBanners
import com.example.waywayapp.ui.user.home.mock.mockServices
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
    private var searchJob: Job? = null

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        _uiState.update {
            it.copy(
                services = mockServices,
                banners = mockBanners
            )
        }
    }

    fun onSearchChange(value: String) {
        _uiState.update {
            it.copy(searchText = value)
        }

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
                _uiState.update {
                    it.copy(searchSuggestions = results.take(6))
                }
            }.onFailure {
                _uiState.update {
                    it.copy(searchSuggestions = emptyList())
                }
            }
        }
    }

    fun clearSearchSuggestions() {
        searchJob?.cancel()
        _uiState.update {
            it.copy(searchSuggestions = emptyList())
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
            .replace('đ', 'd')
            .replace('Đ', 'D')
    }

}
