package com.example.waywayapp.ui.user.booking

data class BookingState(

    val currentStep: BookingStep = BookingStep.SEARCH,
    val searchQuery: String = "",
    val searchResults: List<PlaceResult> = emptyList(),



    )
enum class BookingStep {
    SEARCH, MAP_PICK, CHECKOUT
}