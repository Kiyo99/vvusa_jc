package com.example.vvusa_jc.ui.hostel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.vvusa_jc.data.model.HostelAnnouncement
import com.example.vvusa_jc.data.model.RoomInfo

@Composable
fun UserRoomStatusCard(
    roomInfo: RoomInfo,
    userName: String,
    announcements: List<HostelAnnouncement>,
    complaintTitle: String,
    complaintMessage: String,
    onTitleChange: (String) -> Unit,
    onMessageChange: (String) -> Unit,
    onSubmitComplaint: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        WelcomeHeader(userName, roomInfo.hostelName)

        Spacer(modifier = Modifier.height(24.dp))

        AnnouncementsSection(announcements)

        Spacer(modifier = Modifier.height(24.dp))

        RoomInfoDisplay(roomInfo.roomNumber)

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn {
            item {
                ComplaintForm(
                    complaintTitle = complaintTitle,
                    complaintMessage = complaintMessage,
                    onTitleChange = onTitleChange,
                    onMessageChange = onMessageChange,
                    onSubmitComplaint = onSubmitComplaint
                )
            }
        }
    }
}

@Composable
fun WelcomeHeader(userName: String, hostelName: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Welcome $userName",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "to $hostelName",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun AnnouncementsSection(announcements: List<HostelAnnouncement>) {
    Text(
        text = "Announcements",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.heightIn(max = 200.dp)
    ) {
        items(announcements) { announcement ->
            AnnouncementCard(announcement = announcement)
        }
    }
}

@Composable
fun RoomInfoDisplay(roomNumber: String) {
    Text(
        text = "Your Room Number",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Room $roomNumber",
        style = MaterialTheme.typography.headlineMedium,
        color = Color.Red,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun ComplaintForm(
    complaintTitle: String,
    complaintMessage: String,
    onTitleChange: (String) -> Unit,
    onMessageChange: (String) -> Unit,
    onSubmitComplaint: () -> Unit
) {
    Text(
        text = "Make a Complaint",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = complaintTitle,
        onValueChange = onTitleChange,
        label = { Text("Title") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    )

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = complaintMessage,
        onValueChange = onMessageChange,
        label = { Text("Message") },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp),
        shape = RoundedCornerShape(8.dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick = onSubmitComplaint,
        enabled = complaintTitle.isNotBlank() && complaintMessage.isNotBlank(),
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text("SEND")
    }
}

@Composable
fun AnnouncementCard(announcement: HostelAnnouncement) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = announcement.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = announcement.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Read more",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}