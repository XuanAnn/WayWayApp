import com.example.waywayapp.R
import com.example.waywayapp.ui.user.booking.food.model.FoodBannerUiModel
import com.example.waywayapp.ui.user.booking.food.model.FoodCategoryUiModel
import com.example.waywayapp.ui.user.booking.food.model.FoodItemUiModel
import com.example.waywayapp.ui.user.home.model.FoodPreviewUiModel

val foodList = listOf(
    FoodItemUiModel(
        id = 1,
        name = "Burger bò phô mai",
        description = "Burger bò phô mai thơm béo",
        store = "WayWay Food",
        price = 39000.0,
        distance = "1.2 km",
        rating = 4.8,
        imageRes = R.drawable.banner_promo1,
        badge = "Hot"
    ),
    FoodItemUiModel(
        id = 2,
        name = "Trà sữa trân châu",
        description = "Ngọt vừa, thêm trân châu",
        store = "Milk Tea House",
        price = 29000.0,
        distance = "0.8 km",
        rating = 4.9,
        imageRes = R.drawable.banner_promo1,
        badge = "Mới"
    ),
    FoodItemUiModel(
        id = 3,
        name = "Cơm gà sốt cay",
        description = "Cơm nóng, gà mềm, sốt cay",
        store = "Cơm ngon Đà Nẵng",
        price = 45000.0,
        distance = "2.1 km",
        rating = 4.7,
        imageRes = R.drawable.banner_promo1,
        badge = "Sale"
    )

)
val foodCategories = listOf(
    FoodCategoryUiModel(
        name = "Cơm",
        icon = R.drawable.rice
    ),
    FoodCategoryUiModel(
        name = "Burger",
        icon = R.drawable.rice

    ),
    FoodCategoryUiModel(
        name = "Trà sữa",
        icon = R.drawable.rice
    ),
    FoodCategoryUiModel(
        name = "Phở",
        icon = R.drawable.rice

    ),

)
val foodBanners = listOf(
    FoodBannerUiModel(R.drawable.banner_promo1),
    FoodBannerUiModel(R.drawable.banner_promo1),
    FoodBannerUiModel(R.drawable.banner_promo1)
)
val mockFoods = listOf(
    FoodPreviewUiModel(
        id = 1,
        name = "Trà sữa Sài Gòn",
        price = "từ 35.000đ",
        badge = "Mới",
        imageRes = R.drawable.banner_promo1
    ),

    FoodPreviewUiModel(
        id = 2,
        name = "Cơm thố Nhật",
        price = "từ 55.000đ",
        badge = "Hot",
        imageRes = R.drawable.banner_promo1
    )
)