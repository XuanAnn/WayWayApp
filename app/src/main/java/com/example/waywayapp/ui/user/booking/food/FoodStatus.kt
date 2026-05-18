package com.example.waywayapp.ui.user.booking.food.model

enum class FoodStatus {

    /*
    Người dùng đang chọn món
     */
    BROWSING,

    /*
    Đang xem giỏ hàng
     */
    CART,

    /*
    Đã bấm đặt hàng
    Hệ thống đang tìm tài xế
     */
    FINDING_DRIVER,

    /*
    Tài xế đã nhận đơn
     */
    DRIVER_ACCEPTED,

    /*
    Tài xế đang tới quán lấy món
     */
    PICKING_ORDER,

    /*
    Tài xế đã lấy món
    Đơn đang giao
     */
    DELIVERING,

    /*
    Đơn hoàn thành
     */
    COMPLETED,

    /*
    Đơn bị huỷ
     */
    CANCELLED
}