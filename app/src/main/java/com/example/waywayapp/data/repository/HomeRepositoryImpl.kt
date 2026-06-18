package com.example.waywayapp.data.repository

import com.example.waywayapp.core.firebase.FirestoreProvider
import com.example.waywayapp.domain.model.Service
import com.example.waywayapp.domain.repository.HomeRepository
import kotlinx.coroutines.tasks.await

class HomeRepositoryImpl: HomeRepository {
    private val firestore = FirestoreProvider.db

    override suspend fun loadServices(): List<Service> {
        val remoteServices = runCatching {
            firestore.collection("home_services")
                .get()
                .await()
                .documents
                .mapNotNull { document ->
                    val title = document.getString("title").orEmpty()
                    val type = document.getString("type").orEmpty()
                    if(title.isBlank() || type.isBlank()) return@mapNotNull null
                    Service(
                        title = title,
                        type = type

                    )
                }
        }.getOrElse {
            emptyList()
        }
        return mergeWithDefaultServices(remoteServices)
    }

    private fun defaultServices(): List<Service> {
        return listOf(
            Service(title = "Bike", type = "bike" ),
            Service(title = "Car", type = "car"),
            Service(title = "Express", type = "express")
        )
    }
    private fun mergeWithDefaultServices(
        remoteServices: List<Service>
    ): List<Service> {
        if (remoteServices.isEmpty()) return defaultServices()

        // 1. Chỉ gọi hàm tạo danh sách mặc định đúng 1 lần duy nhất
        val defaults = defaultServices()

        // 2. Gom nhóm dữ liệu từ server theo 'type' (viết thường) để tìm kiếm nhanh
        val remoteByType = remoteServices.associateBy { it.type.lowercase() }

        // 3. Cập nhật dữ liệu từ server vào danh sách mặc định
        val mergedDefaults = defaults.map { fallback ->
            remoteByType[fallback.type.lowercase()] ?: fallback
        }

        // 4. Tạo bộ Set chứa các 'type' mặc định để kiểm tra phần tử thừa với tốc độ O(1)
        val defaultTypes = defaults.map { it.type.lowercase() }.toSet()

        // 5. Lọc ra các dịch vụ mới từ server (không nằm trong tập hợp mặc định)
        val extraServices = remoteServices.filter { remote ->
            remote.type.lowercase() !in defaultTypes
        }

        return mergedDefaults + extraServices
    }

}
