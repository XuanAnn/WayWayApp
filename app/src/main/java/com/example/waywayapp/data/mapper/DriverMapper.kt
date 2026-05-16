package com.example.waywayapp.data.mapper

import com.example.waywayapp.data.remote.dto.DriverDto
import com.example.waywayapp.domain.model.Driver

fun DriverDto.toDomain(): Driver {
    return Driver(
        fullName = full_name,
        vehicleType = vehicle_type
    )
}
