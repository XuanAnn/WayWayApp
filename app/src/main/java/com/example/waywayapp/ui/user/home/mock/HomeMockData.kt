package com.example.waywayapp.ui.user.home.mock

import com.example.waywayapp.R
import com.example.waywayapp.ui.user.home.model.BannerUiModel
import com.example.waywayapp.ui.user.home.model.FoodPreviewUiModel
import com.example.waywayapp.ui.user.home.model.ServiceUiModel

val mockServices = listOf(
    ServiceUiModel("Xe máy", "bike", R.drawable.bike_icon),
    ServiceUiModel("Đồ ăn", "food", R.drawable.food_icon),
    ServiceUiModel("Ô tô", "car", R.drawable.car_icon),
    ServiceUiModel("Giao hàng", "express", R.drawable.express_icon)
)

val mockBanners = listOf(
    BannerUiModel("Khuyến mãi", "Giảm 30% đơn đầu tiên", R.drawable.food_banner),
    BannerUiModel("Hot deal", "Freeship cuối tuần", R.drawable.banner_promo1),
)

val mockFoods = listOf(
    FoodPreviewUiModel(1,"Trà sữa Sài Gòn", "từ 35.000đ", "Mới", R.drawable.rice),
    FoodPreviewUiModel(2,"Cơm thố Nhật", "từ 55.000đ", "Mới", R.drawable.rice),
    FoodPreviewUiModel(3,"Burger bò phô mai", "từ 39.000đ", "Hot", R.drawable.rice)
)