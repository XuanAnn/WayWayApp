package com.example.waywayapp.ui.user.booking

data class BookingState(

    val currentStep: BookingStep = BookingStep.PICK_LOCATION,
    val searchQuery: String = "",
//    val searchResults: List<PlaceResult> = emptyList(),



    )
enum class BookingStep {
    PICK_LOCATION,  // Đang nhập điểm đón/đến hoặc kéo bản đồ
    SELECT_RIDE,    // Đang chọn loại xe (Bike, Car, Plus...) và xem giá
    FINDING_DRIVER, // Màn hình chờ (Đang tìm tài xế...)
    ON_TRIP         // Đã có tài xế, hiển thị thông tin tài xế và theo dõi
}