package com.example.vvusa_jc.ui.workstudy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkstudyScreen() {
    val coroutineScope = rememberCoroutineScope()
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()

    var isLoading by remember { mutableStateOf(true) }
    var positions by remember { mutableStateOf<List<WorkPosition>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Show application status dialog
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    var dialogTitle by remember { mutableStateOf("") }

    // Fetch work positions from Firestore
    LaunchedEffect(Unit) {
        try {
            isLoading = true

            // Try to fetch from Firestore
            val positionsSnapshot = firestore.collection("workPositions")
                .get()
                .await()

            val fetchedPositions = positionsSnapshot.documents.mapNotNull { doc ->
                val data = doc.data
                if (data != null) {
                    WorkPosition(
                        id = doc.id,
                        title = data["title"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        department = data["department"] as? String ?: "",
                        hoursPerWeek = (data["hoursPerWeek"] as? Long)?.toInt() ?: 0
                    )
                } else null
            }

            positions = if (fetchedPositions.isNotEmpty()) {
                fetchedPositions
            } else {
                // Use sample data if Firestore has no positions
                sampleWorkPositions
            }

            isLoading = false
        } catch (e: Exception) {
            errorMessage = "Error loading positions: ${e.message}"
            positions = sampleWorkPositions
            isLoading = false
        }
    }

    // Submit application function
    fun submitApplication(position: WorkPosition) {
        coroutineScope.launch {
            try {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    val userDoc = firestore.collection("Users").document(userId).get().await()
                    val firstName = userDoc.getString("First name") ?: "Unknown"
                    val lastName = userDoc.getString("Last name") ?: "Unknown"
                    val studentId = userDoc.getString("ID") ?: "Unknown"
                    val studentEmail = userDoc.getString("email") ?: "Unknown"

                    val studentName = "$firstName $lastName"

                    // Create application document
                    val application = hashMapOf(
                        "positionId" to position.id,
                        "positionTitle" to position.title,
                        "userId" to userId,
                        "studentName" to studentName,
                        "studentId" to studentId,
                        "studentEmail" to studentEmail,
                        "status" to "pending",
                        "appliedAt" to System.currentTimeMillis()
                    )

                    // Add to applications collection
                    firestore.collection("workApplications")
                        .add(application)
                        .await()

                    // Show success dialog
                    dialogTitle = "Application Submitted"
                    dialogMessage = "Your application for ${position.title} has been submitted successfully!"
                    showDialog = true
                }
            } catch (e: Exception) {
                // Show error dialog
                dialogTitle = "Application Failed"
                dialogMessage = "Failed to submit application: ${e.message}"
                showDialog = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "Gain experience, apply to workstudy positions",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(positions) { position ->
                    WorkPositionCard(
                        position = position,
                        onApply = { submitApplication(position) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Application status dialog
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(text = dialogTitle) },
                text = { Text(text = dialogMessage) },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkPositionCard(
    position: WorkPosition,
    onApply: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = position.title,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = position.description,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.9f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Department: ${position.department}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )

                    Text(
                        text = "Hours/Week: ${position.hoursPerWeek}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                Button(
                    onClick = onApply,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Apply",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Data class for work positions
data class WorkPosition(
    val id: String,
    val title: String,
    val description: String,
    val department: String,
    val hoursPerWeek: Int
)

// Sample data for work positions
val sampleWorkPositions = listOf(
    WorkPosition(
        id = "1",
        title = "Lab Assistant",
        description = "As a lab assistant, you are responsible for ensuring the lab rules are properly followed.",
        department = "Science Department",
        hoursPerWeek = 10
    ),
    WorkPosition(
        id = "2",
        title = "Weeding",
        description = "As a weeder, you are responsible for ensuring the plants are properly trimmed.",
        department = "Grounds Maintenance",
        hoursPerWeek = 15
    ),
    WorkPosition(
        id = "3",
        title = "Office Secretary",
        description = "As an office secretary, you will be assigned to your faculty office and perform duties befitting a secretary.",
        department = "Administration",
        hoursPerWeek = 20
    ),
    WorkPosition(
        id = "4",
        title = "Library Assistant",
        description = "Assist with book shelving, checkout, and maintaining library organization.",
        department = "Library",
        hoursPerWeek = 12
    ),
    WorkPosition(
        id = "5",
        title = "IT Support",
        description = "Help students and staff with basic computer and technology issues.",
        department = "IT Department",
        hoursPerWeek = 15
    )
)