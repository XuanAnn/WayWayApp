package com.example.waywayapp.ui.user.rating

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.waywayapp.data.model.Ride
import com.example.waywayapp.data.repository.RideRepository
import com.example.waywayapp.ui.theme.AppBg
import com.example.waywayapp.ui.theme.TextDark
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
// Màn hình user đánh giá tài xế sau khi chuyến xe hoàn thành.
fun RideRatingScreen(
    rideId: String,
    onBackClick: () -> Unit,
    onDone: () -> Unit,
    repository: RideRepository = RideRepository()
) {
    // Theo dõi ride realtime để lấy thông tin tài xế và giá chuyến.
    val ride by repository.observeRide(rideId).collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    // Điểm đánh giá user chọn, mặc định 5 sao.
    var rating by remember { mutableIntStateOf(5) }
    // Nội dung nhận xét gửi kèm vào document ride.
    var review by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Nếu ride đã có đánh giá trước đó thì nạp lại để user thấy.
    LaunchedEffect(ride?.id) {
        ride?.let {
            if (it.userRating > 0) rating = it.userRating
            if (it.userReview.isNotBlank()) review = it.userReview
        }
    }

    Scaffold(
        containerColor = AppBg,
        topBar = {
            TopAppBar(
                title = {
                    Text("Danh gia tai xe", fontWeight = FontWeight.ExtraBold, color = TextDark)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBg)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(18.dp)
        ) {
            val currentRide = ride
            if (currentRide == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                RatingContent(
                    ride = currentRide,
                    rating = rating,
                    review = review,
                    isSaving = isSaving,
                    error = error,
                    onRatingChange = { rating = it },
                    onReviewChange = { review = it },
                    onSubmit = {
                        scope.launch {
                            isSaving = true
                            error = null
                            // Lưu rating/review vào Firestore và cập nhật điểm trung bình driver.
                            runCatching {
                                repository.submitRideRating(
                                    rideId = rideId,
                                    rating = rating,
                                    review = review
                                )
                            }.onSuccess {
                                isSaving = false
                                onDone()
                            }.onFailure {
                                isSaving = false
                                error = it.localizedMessage ?: "Khong luu duoc danh gia"
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
// Nội dung form đánh giá gồm thông tin chuyến, sao và nhận xét.
private fun RatingContent(
    ride: Ride,
    rating: Int,
    review: String,
    isSaving: Boolean,
    error: String?,
    onRatingChange: (Int) -> Unit,
    onReviewChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEAF4FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ride.driverName.take(1).ifBlank { "D" },
                            color = Color(0xFF0B65C2),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.size(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = ride.driverName.ifBlank { "Tai xe" },
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDark,
                            fontSize = 18.sp
                        )
                        Text(
                            text = ride.driverPlate.ifBlank { ride.driverPhone.ifBlank { "WayWay Driver" } },
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                    Text(
                        text = NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(ride.price),
                        color = Color(0xFF00A85A),
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
                Text("Chuyen di cua ban da hoan thanh", fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(modifier = Modifier.height(6.dp))
                Text(ride.dropoffAddress.ifBlank { "Diem den chua cap nhat" }, color = Color.Gray, fontSize = 13.sp)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Ban danh gia chuyen di nay the nao?", fontWeight = FontWeight.ExtraBold, color = TextDark)
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..5).forEach { star ->
                        Icon(
                            imageVector = if (star <= rating) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            tint = Color(0xFFFFB300),
                            modifier = Modifier
                                .size(42.dp)
                                .clickable { onRatingChange(star) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
                OutlinedTextField(
                    value = review,
                    onValueChange = onReviewChange,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(18.dp),
                    placeholder = { Text("Nhan xet them ve tai xe...") }
                )
            }
        }

        error?.let {
            Text(it, color = Color(0xFFD93025), fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onSubmit,
            enabled = !isSaving,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B14F))
        ) {
            if (isSaving) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Gui danh gia", color = Color.White, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}
