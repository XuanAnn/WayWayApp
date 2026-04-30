package com.example.waywayapp.ui.user.booking.bike

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OsrmService {
    @GET("route/v1/driving/{coordinates}")
    suspend fun getRoute(
        @Path("coordinates") coordinates: String,
        @Query("overview") overview: String = "full",
        @Query("geometries") geometries: String = "polyline",
        @Query("steps") steps: Boolean = true
    ): OsrmResponse
}

data class OsrmResponse(
    val routes: List<OsrmRoute>
)

data class OsrmRoute(
    val geometry: String,
    val distance: Double,
    val duration: Double
)
