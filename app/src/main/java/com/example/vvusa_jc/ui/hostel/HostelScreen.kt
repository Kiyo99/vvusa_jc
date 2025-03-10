package com.example.vvusa_jc.ui.hostel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HostelScreen(
    viewModel: HostelViewModel = viewModel(),
    onRoomSelection: (String, String, String) -> Unit,
    onPayment: (String, String, String, Int) -> Unit,
    onViewStatus: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Main content based on UI state
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Content based on state
        when (uiState) {
            is HostelUiState.Loading -> {
                LoadingIndicator()
            }

            is HostelUiState.Error -> {
                ErrorView(
                    errorMessage = (uiState as HostelUiState.Error).message,
                    onRetry = { viewModel.loadUserData() }
                )
            }

            is HostelUiState.RoomBooked -> {
                UserRoomStatusCard(
                    roomInfo = viewModel.userRoomInfo!!,
                    userName = viewModel.userName,
                    announcements = viewModel.hostelAnnouncements,
                    complaintTitle = viewModel.complaintTitle,
                    complaintMessage = viewModel.complaintMessage,
                    onTitleChange = viewModel::updateComplaintTitle,
                    onMessageChange = viewModel::updateComplaintMessage,
                    onSubmitComplaint = viewModel::submitComplaint
                )
            }

            is HostelUiState.SelectHostel -> {
                // Header
                HostelHeader()

                Spacer(modifier = Modifier.height(24.dp))

                HostelSelection(
                    gender = viewModel.userGender ?: "male",
                    onHostelSelected = viewModel::selectHostel
                )
            }

            is HostelUiState.SelectFloor -> {
                // Header
                HostelHeader()

                Spacer(modifier = Modifier.height(24.dp))

                FloorSelection(
                    hostelName = viewModel.selectedHostel ?: "",
                    onFloorSelected = viewModel::selectFloor,
                    onBack = viewModel::goBackToHostels
                )
            }

            is HostelUiState.SelectRoom -> {
                // Header
                HostelHeader()

                Spacer(modifier = Modifier.height(24.dp))

                RoomSelection(
                    rooms = viewModel.roomData,
                    hostelName = viewModel.selectedHostel ?: "",
                    floorName = viewModel.selectedFloor ?: "",
                    onRoomSelected = { roomId, roomNumber ->
                        onRoomSelection(viewModel.selectedHostel!!, viewModel.selectedFloor!!, roomNumber)
                        viewModel.bookRoom(roomId, roomNumber)
                    },
                    onPaymentSelected = { roomId, roomNumber, roomType ->
                        onPayment(viewModel.selectedHostel!!, viewModel.selectedFloor!!, roomNumber,
                            if (roomType == 4) 600 else 900)
                        viewModel.processPayment(roomId, roomNumber, roomType)
                    },
                    onBack = viewModel::goBackToFloors
                )
            }
        }

        // Option to view room status if in selection flow
        if (uiState !is HostelUiState.RoomBooked && uiState !is HostelUiState.Loading && uiState !is HostelUiState.Error) {
            // Header
            HostelHeader()

            Spacer(modifier = Modifier.height(24.dp))

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onViewStatus,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("I WANT TO VIEW MY ROOM STATUS")
            }
        }
    }
}

@Composable
fun HostelHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "VVU Hostel",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Booking hostels on campus has never been this easy. Select a payment plan and follow the prompts.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorView(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Error",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}