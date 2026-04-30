import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.waywayapp.ui.user.booking.bike.BikeState
import com.example.waywayapp.ui.user.booking.bike.BikeViewModel
import com.example.waywayapp.ui.user.booking.bike.BookingStatus
import com.example.waywayapp.ui.user.booking.bike.ui.BookingUI
import com.example.waywayapp.ui.user.booking.bike.ui.CompletedUI
import com.example.waywayapp.ui.user.booking.bike.ui.OnTripUI
import com.example.waywayapp.ui.user.booking.bike.ui.WaitingUI
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.*

@Composable
fun BikeBookingScreen(
    viewModel: BikeViewModel = viewModel(),
) {
    val uiState = viewModel.uiState.collectAsState().value

    when (uiState.status) {
        BookingStatus.SELECTING -> BookingUI(viewModel)
        BookingStatus.WAITING -> WaitingUI()
        BookingStatus.ON_TRIP -> OnTripUI()
        BookingStatus.COMPLETED -> CompletedUI()

    }
}