package com.example.vvusa_jc.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await


//kio.favour@st.vvu.edu.gh
//Tifto123
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    var userName by remember { mutableStateOf("") }
    var temperature by remember { mutableStateOf(0.0) }
    var announcements by remember { mutableStateOf<List<Announcement>>(emptyList()) }

    // Fetch user details
    LaunchedEffect(auth.currentUser?.uid) {
        auth.currentUser?.uid?.let { userId ->
            try {
                val userDoc = firestore.collection("Users").document(userId).get().await()
                userName = userDoc.getString("First name") ?: ""

                // todo Fetch weather data (in a real app, you would use a weather API)
                temperature = 27.5 // Example temperature

                // Fetch announcements
                val announcementsSnapshot = firestore.collection("announcements")
                    .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(5)
                    .get()
                    .await()

                announcements = announcementsSnapshot.documents.mapNotNull { doc ->
                    doc.data?.let {
                        Announcement(
                            id = doc.id,
                            title = it["title"] as? String ?: "",
                            content = it["content"] as? String ?: "",
                            timestamp = (it["timestamp"] as? Long) ?: 0L
                        )
                    }
                }

                // If no announcements from database, use sample data
                if (announcements.isEmpty()) {
                    announcements = sampleAnnouncements
                }
            } catch (e: Exception) {
                // Use sample data if fetch fails
                temperature = 26.8
                announcements = sampleAnnouncements
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Welcome Header
            item {
                WelcomeHeader(
                    userName = userName,
                    temperature = temperature
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // What's New Section
            item {
                Text(
                    text = "What's new?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Announcements
            items(announcements.size) { index ->
                AnnouncementCard(announcement = announcements[index])
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun WelcomeHeader(
    userName: String,
    temperature: Double,
    modifier: Modifier = Modifier
) {
    val greeting = when (java.time.LocalTime.now().hour) {
        in 0..11 -> "Good morning"
        in 12..17 -> "Good afternoon"
        else -> "Good evening"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "$greeting, $userName",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Student at VVU",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${String.format("%.1f", temperature)}°C",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnouncementCard(
    announcement: Announcement,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
        ),
        onClick = { /* Navigate to full announcement */ }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = announcement.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = announcement.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Read more",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Sample data
data class Announcement(
    val id: String,
    val title: String,
    val content: String,
    val timestamp: Long
)

val sampleAnnouncements = listOf(
    Announcement(
        id = "1",
        title = "Dear Students",
        content = "Letter from the Dean of Student Life and Services",
        timestamp = System.currentTimeMillis()
    ),
    Announcement(
        id = "2",
        title = "Vacation!",
        content = "School vacates on December 23rd, 2023.",
        timestamp = System.currentTimeMillis() - 86400000 // 1 day ago
    ),
    Announcement(
        id = "3",
        title = "Hall Week",
        content = "Hall week celebration kicks off this Sunday. All students are invited to participate.",
        timestamp = System.currentTimeMillis() - 172800000 // 2 days ago
    )
)