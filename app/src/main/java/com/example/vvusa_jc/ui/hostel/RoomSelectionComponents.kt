package com.example.vvusa_jc.ui.hostel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.vvusa_jc.data.model.RoomData

@Composable
fun RoomSelection(
    rooms: List<RoomData>,
    hostelName: String,
    floorName: String,
    onRoomSelected: (String, String) -> Unit,
    onPaymentSelected: (String, String, Int) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Back button or breadcrumb
        TextButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Text("← Back to Floors")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Available Rooms on $floorName - $hostelName",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        if (rooms.isEmpty()) {
            NoRoomsAvailable()
        } else {
            RoomsAvailableContent(
                rooms = rooms,
                onRoomSelected = onRoomSelected,
                onPaymentSelected = onPaymentSelected
            )
        }
    }
}

@Composable
fun NoRoomsAvailable() {
    Spacer(modifier = Modifier.height(32.dp))

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No available rooms on this floor",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Please try another floor or hostel",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun RoomsAvailableContent(
    rooms: List<RoomData>,
    onRoomSelected: (String, String) -> Unit,
    onPaymentSelected: (String, String, Int) -> Unit
) {
    Spacer(modifier = Modifier.height(16.dp))

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        // Room cards with booking options
        PaymentOptionsCard(
            rooms = rooms,
            onPaymentSelected = onPaymentSelected
        )

        // List of specific available rooms
        Text(
            text = "Available Rooms:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(rooms) { room ->
                if (room.available) {
                    RoomCard(
                        roomNumber = room.number,
                        capacity = room.capacity,
                        maxCapacity = room.maxCapacity,
                        onClick = { onRoomSelected(room.id, room.number) }
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentOptionsCard(
    rooms: List<RoomData>,
    onPaymentSelected: (String, String, Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Select a payment plan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4 in a room
            Button(
                onClick = {
                    val room = rooms.firstOrNull { it.maxCapacity == 4 && it.available }
                    if (room != null) {
                        onPaymentSelected(room.id, room.number, 4)
                    }
                },
                enabled = rooms.any { it.maxCapacity == 4 && it.available },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("I WANT TO PAY FOR ACCOMMODATION (4 IN A ROOM)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2 in a room
            Button(
                onClick = {
                    val room = rooms.firstOrNull { it.maxCapacity == 2 && it.available }
                    if (room != null) {
                        onPaymentSelected(room.id, room.number, 2)
                    }
                },
                enabled = rooms.any { it.maxCapacity == 2 && it.available },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("I WANT TO PAY FOR ACCOMMODATION (2 IN A ROOM)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Book a specific room
            Button(
                onClick = { /* No action needed, rooms list is shown below */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("I WANT TO BOOK A ROOM")
            }
        }
    }
}

@Composable
fun RoomCard(
    roomNumber: String,
    capacity: Int,
    maxCapacity: Int,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Room $roomNumber",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "${maxCapacity - capacity} beds available out of $maxCapacity",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Book")
            }
        }
    }
}