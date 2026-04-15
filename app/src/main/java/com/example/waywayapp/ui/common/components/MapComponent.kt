package com.example.waywayapp.ui.common.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

@Composable
fun MapCompose() {
    val singapore = LatLng(1.35, 103.87)
    val singapore1 = LatLng(1.30, 103.87) // Chỉnh lại giá trị 1.3 thành 1.30 để tránh nhầm lẫn format

    val singaporeMarkerState = rememberMarkerState(position = singapore)
    val singaporeMarkerState1 = rememberMarkerState(position = singapore1)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(singapore, 11f)
    }

    // State lưu trữ danh sách các điểm để vẽ đường
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }

    // Gọi API trong background thread khi component khởi tạo
    LaunchedEffect(singapore, singapore1) {
        routePoints = fetchRouteFromOSRM(singapore, singapore1)
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = singaporeMarkerState,
            title = "Điểm A",
            snippet = "Marker in Singapore"
        )
        Marker(
            state = singaporeMarkerState1,
            title = "Điểm B",
            snippet = "Marker in Singapore"
        )

        // Vẽ tuyến đường khi dữ liệu sẵn sàng
        if (routePoints.isNotEmpty()) {
            Polyline(
                points = routePoints,
                color = Color.Blue,
                width = 10f
            )
        }
    }
}

/**
 * Gửi yêu cầu HTTP đến OSRM API và trả về danh sách tọa độ.
 */
suspend fun fetchRouteFromOSRM(start: LatLng, end: LatLng): List<LatLng> {
    return withContext(Dispatchers.IO) {
        try {
            // Cú pháp OSRM: longitude,latitude
            val url = "https://router.project-osrm.org/route/v1/driving/${start.longitude},${start.latitude};${end.longitude},${end.latitude}?overview=full&geometries=polyline"
            val response = URL(url).readText()

            val jsonObject = JSONObject(response)
            val routes = jsonObject.getJSONArray("routes")

            if (routes.length() > 0) {
                val geometry = routes.getJSONObject(0).getString("geometry")
                return@withContext decodePolyline(geometry)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext emptyList()
    }
}

/**
 * Thuật toán giải mã Google Encoded Polyline algorithm string.
 * Có thể thay thế bằng hàm PolyUtil.decode() nếu sử dụng thư viện android-maps-utils.
 */
fun decodePolyline(encoded: String): List<LatLng> {
    val poly = ArrayList<LatLng>()
    var index = 0
    val len = encoded.length
    var lat = 0
    var lng = 0

    while (index < len) {
        var b: Int
        var shift = 0
        var result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lat += dlat

        shift = 0
        result = 0
        do {
            b = encoded[index++].code - 63
            result = result or (b and 0x1f shl shift)
            shift += 5
        } while (b >= 0x20)
        val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
        lng += dlng

        val p = LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
        poly.add(p)
    }
    return poly
}