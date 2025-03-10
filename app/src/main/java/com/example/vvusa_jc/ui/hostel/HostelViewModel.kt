package com.example.vvusa_jc.ui.hostel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vvusa_jc.data.model.HostelAnnouncement
import com.example.vvusa_jc.data.model.RoomData
import com.example.vvusa_jc.data.model.RoomInfo
import com.example.vvusa_jc.data.model.sampleAnnouncements
import com.example.vvusa_jc.data.model.sampleRooms
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HostelViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // UI state management
    private val _uiState = MutableStateFlow<HostelUiState>(HostelUiState.Loading)
    val uiState: StateFlow<HostelUiState> = _uiState.asStateFlow()

    // User data
    var userGender by mutableStateOf<String?>(null)
        private set
    var userName by mutableStateOf("")
        private set
    var userRoomInfo by mutableStateOf<RoomInfo?>(null)
        private set

    // Selection state
    var selectedHostel by mutableStateOf<String?>(null)
        private set
    var selectedFloor by mutableStateOf<String?>(null)
        private set
    var roomData by mutableStateOf<List<RoomData>>(emptyList())
        private set
    var showRoomList by mutableStateOf(false)
        private set

    // Announcements
    var hostelAnnouncements by mutableStateOf<List<HostelAnnouncement>>(emptyList())
        private set

    // Form fields for complaint
    var complaintTitle by mutableStateOf("")
        private set
    var complaintMessage by mutableStateOf("")
        private set

    init {
        loadUserData()
    }

    fun loadUserData() {
        viewModelScope.launch {
            try {
                _uiState.value = HostelUiState.Loading

                val userId = auth.currentUser?.uid
                if (userId != null) {
                    // Fetch user profile
                    val userDoc = firestore.collection("Users").document(userId).get().await()
                    userGender = userDoc.getString("gender") ?: "male"
                    userName = userDoc.getString("First name") ?: ""

                    // Check if user has a booked room
                    val bookingsQuery = firestore.collection("hostelBookings")
                        .whereEqualTo("userId", userId)
                        .whereEqualTo("active", true)
                        .get()
                        .await()

                    if (!bookingsQuery.isEmpty) {
                        val booking = bookingsQuery.documents[0]
                        userRoomInfo = RoomInfo(
                            hostelName = booking.getString("hostelName") ?: "",
                            floor = booking.getString("floor") ?: "",
                            roomNumber = booking.getString("roomNumber") ?: ""
                        )
                        loadAnnouncements()
                        _uiState.value = HostelUiState.RoomBooked
                    } else {
                        _uiState.value = HostelUiState.SelectHostel
                    }
                } else {
                    _uiState.value = HostelUiState.Error("User not authenticated")
                }
            } catch (e: Exception) {
                Log.e("HostelViewModel", "Error loading user data", e)
                _uiState.value = HostelUiState.Error("Failed to load user data: ${e.message}")
                userGender = "male"
            }
        }
    }

    fun selectHostel(hostelName: String) {
        selectedHostel = hostelName
        _uiState.value = HostelUiState.SelectFloor
    }

    fun selectFloor(floorName: String) {
        selectedFloor = floorName
        loadRooms()
    }

    fun goBackToHostels() {
        selectedHostel = null
        selectedFloor = null
        showRoomList = false
        _uiState.value = HostelUiState.SelectHostel
    }

    fun goBackToFloors() {
        selectedFloor = null
        showRoomList = false
        _uiState.value = HostelUiState.SelectFloor
    }

    private fun loadRooms() {
        viewModelScope.launch {
            try {
                _uiState.value = HostelUiState.Loading

                val hostelName = selectedHostel ?: return@launch
                val floorName = selectedFloor ?: return@launch

                val roomsRef = firestore.collection("Hostel")
                    .document(hostelName)
                    .collection(floorName)
                    .get()
                    .await()

                val rooms = roomsRef.documents.mapNotNull { doc ->
                    val capacity = doc.getLong("Capacity")?.toInt() ?: 0
                    val maxCapacity = doc.getLong("maxCapacity")?.toInt() ?: 4
                    val roomNumber = doc.getString("Name") ?: doc.id
                    val available = doc.getBoolean("Availability") ?: (capacity < maxCapacity)

                    if (available) {
                        RoomData(
                            id = doc.id,
                            number = roomNumber,
                            capacity = capacity,
                            maxCapacity = maxCapacity,
                            available = capacity < maxCapacity
                        )
                    } else null
                }

                roomData = rooms
                showRoomList = true
                _uiState.value = HostelUiState.SelectRoom
            } catch (e: Exception) {
                Log.e("HostelViewModel", "Error loading rooms", e)
                roomData = sampleRooms
                showRoomList = true
                _uiState.value = HostelUiState.SelectRoom
            }
        }
    }

    private fun loadAnnouncements() {
        viewModelScope.launch {
            try {
                val hostelName = userRoomInfo?.hostelName ?: return@launch

                val announcementsRef = firestore.collection("hostelAnnouncements")
                    .whereEqualTo("hostel", hostelName)
                    .orderBy("timestamp")
                    .limit(5)
                    .get()
                    .await()

                val announcements = announcementsRef.documents.mapNotNull { doc ->
                    HostelAnnouncement(
                        title = doc.getString("title") ?: "",
                        message = doc.getString("message") ?: ""
                    )
                }

                hostelAnnouncements = if (announcements.isEmpty()) {
                    sampleAnnouncements
                } else {
                    announcements
                }
            } catch (e: Exception) {
                Log.e("HostelViewModel", "Error loading announcements", e)
                hostelAnnouncements = sampleAnnouncements
            }
        }
    }

    fun bookRoom(roomId: String, roomNumber: String) {
        val hostelName = selectedHostel ?: return
        val floorName = selectedFloor ?: return

        viewModelScope.launch {
            try {
                _uiState.value = HostelUiState.Loading

                val userId = auth.currentUser?.uid ?: return@launch

                // Create booking document
                val bookingData = hashMapOf(
                    "userId" to userId,
                    "hostelName" to hostelName,
                    "floor" to floorName,
                    "roomNumber" to roomNumber,
                    "timestamp" to System.currentTimeMillis(),
                    "active" to true
                )

                // Add booking to Firestore
                firestore.collection("hostelBookings")
                    .add(bookingData)
                    .await()

                // Update room capacity
                val roomRef = firestore.collection("Hostel")
                    .document(hostelName)
                    .collection(floorName)
                    .document(roomId)

                // Get current capacity
                val roomDoc = roomRef.get().await()
                val currentCapacity = roomDoc.getLong("Capacity")?.toInt() ?: 0

                // Increment capacity
                roomRef.update("Capacity", currentCapacity + 1)
                    .await()

                // Update local state
                userRoomInfo = RoomInfo(
                    hostelName = hostelName,
                    floor = floorName,
                    roomNumber = roomNumber
                )

                loadAnnouncements()
                resetSelections()
                _uiState.value = HostelUiState.RoomBooked

            } catch (e: Exception) {
                Log.e("HostelViewModel", "Error booking room", e)
                _uiState.value = HostelUiState.Error("Failed to book room: ${e.message}")
            }
        }
    }

    fun processPayment(roomId: String, roomNumber: String, roomType: Int) {
        // In a real app, this would integrate with a payment gateway
        bookRoom(roomId, roomNumber)
    }

    fun resetSelections() {
        selectedHostel = null
        selectedFloor = null
        showRoomList = false
    }

    fun updateComplaintTitle(title: String) {
        complaintTitle = title
    }

    fun updateComplaintMessage(message: String) {
        complaintMessage = message
    }

    fun submitComplaint() {
        if (complaintTitle.isBlank() || complaintMessage.isBlank()) {
            return
        }

        viewModelScope.launch {
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                val hostelName = userRoomInfo?.hostelName ?: return@launch
                val roomNumber = userRoomInfo?.roomNumber ?: return@launch

                val complaintData = hashMapOf(
                    "userId" to userId,
                    "hostelName" to hostelName,
                    "roomNumber" to roomNumber,
                    "title" to complaintTitle,
                    "message" to complaintMessage,
                    "timestamp" to System.currentTimeMillis(),
                    "status" to "pending"
                )

                firestore.collection("hostelComplaints")
                    .add(complaintData)
                    .await()

                // Reset form fields
                complaintTitle = ""
                complaintMessage = ""
            } catch (e: Exception) {
                Log.e("HostelViewModel", "Error submitting complaint", e)
            }
        }
    }
}

// UI States
sealed class HostelUiState {
    object Loading : HostelUiState()
    object SelectHostel : HostelUiState()
    object SelectFloor : HostelUiState()
    object SelectRoom : HostelUiState()
    object RoomBooked : HostelUiState()
    data class Error(val message: String) : HostelUiState()
}