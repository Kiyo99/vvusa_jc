package com.example.vvusa_jc.data.model

// Data classes
data class RoomData(
    val id: String,
    val number: String,
    val capacity: Int,
    val maxCapacity: Int,
    val available: Boolean
)

data class RoomInfo(
    val hostelName: String,
    val floor: String,
    val roomNumber: String
)

data class HostelAnnouncement(
    val title: String,
    val message: String
)

// Sample data
val sampleRooms = listOf(
    RoomData(id = "1", number = "12", capacity = 2, maxCapacity = 4, available = true),
    RoomData(id = "2", number = "14", capacity = 3, maxCapacity = 4, available = true),
    RoomData(id = "3", number = "16", capacity = 0, maxCapacity = 2, available = true),
    RoomData(id = "4", number = "18", capacity = 1, maxCapacity = 2, available = true)
)

val sampleAnnouncements = listOf(
    HostelAnnouncement(
        title = "Emergency meeting",
        message = "To GF"
    ),
    HostelAnnouncement(
        title = "Hall week",
        message = "Hall week celebration kicks off this Sunday"
    ),
    HostelAnnouncement(
        title = "Cleaning",
        message = "We will be cleaning the hostel this weekend"
    )
)