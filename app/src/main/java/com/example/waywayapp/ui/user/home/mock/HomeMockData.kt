package com.example.waywayapp.ui.user.home.mock

import com.example.waywayapp.R
import com.example.waywayapp.ui.user.home.model.BannerUiModel
import com.example.waywayapp.ui.user.home.model.ServiceUiModel

val mockServices = listOf(
    ServiceUiModel("Xe máy", "bike", R.drawable.bike_icon),
    ServiceUiModel("Ô tô", "car", R.drawable.car_icon),
    ServiceUiModel("Giao hàng", "express", R.drawable.express_icon)
)

val mockBanners = listOf(
    BannerUiModel("Khuyến mãi", "Giảm 30% đơn đầu tiên", R.drawable.banner),
    BannerUiModel("Hot deal", "Freeship cuối tuần", R.drawable.banner_promo1),
)
