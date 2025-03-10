package com.example.vvusa_jc.ui.hostel

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun FloorSelection(
    hostelName: String,
    onFloorSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val floors = listOf("GF", "FF", "SF", "TF") // Ground Floor, First Floor, etc.

    Column(modifier = Modifier.fillMaxWidth()) {
        // Back button or breadcrumb
        TextButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Text("← Back to Hostels")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Select a Floor in $hostelName",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(floors) { floor ->
                FloorCard(
                    floor = floor,
                    onClick = { onFloorSelected(floor) }
                )
            }
        }
    }
}

@Composable
fun FloorCard(
    floor: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when(floor) {
                    "GF" -> "Ground Floor"
                    "FF" -> "First Floor"
                    "SF" -> "Second Floor"
                    "TF" -> "Third Floor"
                    else -> floor
                },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Select",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}