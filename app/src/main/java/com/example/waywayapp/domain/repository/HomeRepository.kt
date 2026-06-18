package com.example.waywayapp.domain.repository

import com.example.waywayapp.domain.model.Service

interface HomeRepository {
    suspend fun loadServices(): List<Service>
}