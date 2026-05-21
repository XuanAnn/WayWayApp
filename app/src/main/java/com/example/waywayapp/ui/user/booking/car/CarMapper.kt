package com.example.waywayapp.ui.user.booking.car

import com.example.waywayapp.ui.user.booking.bike.BikeState
import com.example.waywayapp.ui.user.booking.car.model.CarState

fun CarState.toBikeState(): BikeState {
    return BikeState(
        currentLatLng = currentLatLng,
        pickupLatLng = pickupLatLng,
        dropoffLatLng = dropoffLatLng,
        polylinePoints = polylinePoints,
        pickupAddress = pickupAddress,
        dropoffAddress = dropoffAddress,
        price = price,
        distance = distance,
        duration = duration,
        status = status,
        isLoading = isLoading,
        error = error,
        driverName = driverName,
        driverPlate = driverPlate,
        driverRating = driverRating,
        finalPrice = finalPrice
    )
}