package com.example.serverdrivenui.native.viewmodels

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Attendance scanning states
 */
sealed class AttendanceScanState {
    object Idle : AttendanceScanState()
    object Scanning : AttendanceScanState()
    object Processing : AttendanceScanState()
    data class Success(val message: String) : AttendanceScanState()
    data class Error(val message: String) : AttendanceScanState()
    data class AlreadyCheckedIn(val time: String) : AttendanceScanState()
}

/**
 * ViewModel for QR attendance scanning
 * 
 * This is a pure KMP/Host-side ViewModel (not using Zipline/Redwood).
 * It makes direct Ktor HTTP calls to Supabase, following the same pattern
 * as RealGymService. This keeps composeApp independent of core-data module
 * so that core-data can be updated OTA without requiring app release.
 * 
 * @param httpClient The Ktor HttpClient (same one used elsewhere in the app)
 * @param supabaseUrl Supabase project URL
 * @param supabaseKey Supabase anon key
 * @param userId Current logged-in user ID (from storage)
 * @param accessToken Current access token (from storage)
 * @param onAttendanceMarked Callback when attendance is successfully marked
 */
class AttendanceViewModel(
    private val httpClient: HttpClient,
    private val supabaseUrl: String,
    private val supabaseKey: String,
    private val userId: String?,
    private val accessToken: String?,
    private val onAttendanceMarked: () -> Unit = {}
) {
    private val restUrl = "$supabaseUrl/rest/v1"
    
    private val _state = MutableStateFlow<AttendanceScanState>(AttendanceScanState.Idle)
    val state: StateFlow<AttendanceScanState> = _state.asStateFlow()
    
    private var lastScannedCode: String? = null
    private var processingLock = false
    
    /**
     * Called when QR code is detected by camera
     */
    suspend fun onQrCodeDetected(codeValue: String) {
        // Debounce - don't process same code twice in quick succession
        if (processingLock) {
            println("AttendanceViewModel: Ignoring scan - already processing")
            return
        }
        
        if (codeValue == lastScannedCode && _state.value !is AttendanceScanState.Scanning) {
            println("AttendanceViewModel: Ignoring duplicate code")
            return
        }
        
        processingLock = true
        lastScannedCode = codeValue
        _state.value = AttendanceScanState.Processing
        
        println("AttendanceViewModel: ========== QR SCAN DEBUG ==========")
        println("AttendanceViewModel: Scanned QR code value: '$codeValue'")
        println("AttendanceViewModel: Code length: ${codeValue.length}")
        println("AttendanceViewModel: Code bytes: ${codeValue.toByteArray().contentToString()}")
        println("AttendanceViewModel: userId: $userId")
        println("AttendanceViewModel: accessToken present: ${accessToken != null}")
        println("AttendanceViewModel: supabaseUrl: $supabaseUrl")
        println("AttendanceViewModel: ===================================")
        
        if (userId == null) {
            _state.value = AttendanceScanState.Error("Please login first to mark attendance")
            processingLock = false
            return
        }
        
        try {
            // Step 1: Validate QR code against gym_qr_codes table
            val validation = validateQrCode(codeValue)
            println("AttendanceViewModel: QR validation result: $validation")
            
            if (!validation.isValid) {
                _state.value = AttendanceScanState.Error(
                    validation.errorMessage ?: "Invalid QR code"
                )
                processingLock = false
                return
            }
            
            // Step 2: Check if already checked in today
            val existingCheckIn = getTodayAttendance()
            println("AttendanceViewModel: Existing check-in: $existingCheckIn")
            
            if (existingCheckIn != null) {
                _state.value = AttendanceScanState.AlreadyCheckedIn(existingCheckIn)
                processingLock = false
                return
            }
            
            // Step 3: Mark attendance
            val result = markQrAttendance(validation.qrCodeId)
            println("AttendanceViewModel: Mark attendance result: $result")
            
            when (result) {
                is MarkResult.Success -> {
                    _state.value = AttendanceScanState.Success(
                        "Welcome to ${validation.locationName}! 💪"
                    )
                    // Notify parent to refresh home screen
                    onAttendanceMarked()
                }
                is MarkResult.AlreadyCheckedIn -> {
                    _state.value = AttendanceScanState.AlreadyCheckedIn("today")
                }
                is MarkResult.Error -> {
                    _state.value = AttendanceScanState.Error(result.message)
                }
            }
            
        } catch (e: Exception) {
            println("AttendanceViewModel: Error: ${e.message}")
            _state.value = AttendanceScanState.Error(
                "Failed to mark attendance: ${e.message}"
            )
        } finally {
            processingLock = false
        }
    }
    
    // ============= Direct Supabase API Calls =============
    
    private data class QrValidationResult(
        val isValid: Boolean,
        val qrCodeId: String? = null,
        val locationName: String? = null,
        val errorMessage: String? = null
    )
    
    /**
     * Validate QR code against gym_qr_codes table
     */
    private suspend fun validateQrCode(codeValue: String): QrValidationResult {
        return try {
            val response = httpClient.get("$restUrl/gym_qr_codes") {
                url {
                    parameters.append("code_value", "eq.$codeValue")
                    parameters.append("select", "id,location_name,valid_from,valid_until")
                }
                headers {
                    append("apikey", supabaseKey)
                    append("Authorization", "Bearer ${accessToken ?: supabaseKey}")
                }
            }
            
            if (!response.status.isSuccess()) {
                println("QR validation failed: ${response.status}")
                return QrValidationResult(
                    isValid = false,
                    errorMessage = "Failed to validate QR code"
                )
            }
            
            val body = response.bodyAsText()
            println("QR validation response: $body")
            
            // Parse response - expecting array like [{"id":"...", "location_name":"..."}]
            if (body.contains("[]") || body.trim() == "[]") {
                return QrValidationResult(
                    isValid = false,
                    errorMessage = "This QR code is not from a registered gym"
                )
            }
            
            // Simple regex parsing - extract id and location_name
            val idMatch = Regex("\"id\"\\s*:\\s*\"([^\"]+)\"").find(body)
            val locationMatch = Regex("\"location_name\"\\s*:\\s*\"([^\"]+)\"").find(body)
            
            QrValidationResult(
                isValid = true,
                qrCodeId = idMatch?.groupValues?.get(1),
                locationName = locationMatch?.groupValues?.get(1) ?: "Gym"
            )
        } catch (e: Exception) {
            println("QR validation error: ${e.message}")
            QrValidationResult(
                isValid = false,
                errorMessage = "Network error: ${e.message}"
            )
        }
    }
    
    /**
     * Check if user already checked in today
     * Returns the check-in time if exists, null otherwise
     */
    private suspend fun getTodayAttendance(): String? {
        val today = getCurrentDate()
        
        return try {
            val response = httpClient.get("$restUrl/attendance") {
                url {
                    parameters.append("user_id", "eq.$userId")
                    parameters.append("date", "eq.$today")
                    parameters.append("select", "id,created_at")
                }
                headers {
                    append("apikey", supabaseKey)
                    append("Authorization", "Bearer ${accessToken ?: supabaseKey}")
                }
            }
            
            val body = response.bodyAsText()
            println("Today attendance response: $body")
            
            if (body.contains("[]") || body.trim() == "[]") {
                null // No attendance today
            } else {
                // Extract created_at time
                val timeMatch = Regex("\"created_at\"\\s*:\\s*\"([^\"]+)\"").find(body)
                timeMatch?.groupValues?.get(1)?.let { timestamp ->
                    // Extract just the time portion (HH:MM)
                    val timePart = timestamp.substringAfter("T").take(5)
                    timePart
                } ?: "earlier today"
            }
        } catch (e: Exception) {
            println("Check today attendance error: ${e.message}")
            null
        }
    }
    
    /**
     * Mark attendance via QR scan
     */
    private sealed class MarkResult {
        object Success : MarkResult()
        object AlreadyCheckedIn : MarkResult()
        data class Error(val message: String) : MarkResult()
    }

    /**
     * Mark attendance via QR scan
     */
    private suspend fun markQrAttendance(qrCodeId: String?): MarkResult {
        val today = getCurrentDate()
        val now = getCurrentTimestamp()
        
        return try {
            val body = buildString {
                append("{")
                append("\"user_id\": \"$userId\", ")
                append("\"date\": \"$today\", ")
                append("\"status\": \"present\", ")
                append("\"check_in_method\": \"qr_scan\"")
                if (qrCodeId != null) {
                    append(", \"qr_code_id\": \"$qrCodeId\"")
                }
                append(", \"scanned_at\": \"$now\"")
                append("}")
            }
            
            println("Marking QR attendance: $body")
            println("Using accessToken: ${accessToken?.take(50)}...")
            
            val response = httpClient.post("$restUrl/attendance") {
                contentType(ContentType.Application.Json)
                headers {
                    append("apikey", supabaseKey)
                    append("Authorization", "Bearer ${accessToken ?: supabaseKey}")
                    append("Prefer", "return=minimal")
                }
                setBody(body)
            }
            
            val responseBody = response.bodyAsText()
            println("Response status: ${response.status}")
            println("Response body: $responseBody")
            
            when {
                response.status.isSuccess() -> {
                    println("QR attendance marked successfully")
                    MarkResult.Success
                }
                response.status == HttpStatusCode.Conflict -> {
                    println("QR attendance duplicate (409)")
                    MarkResult.AlreadyCheckedIn
                }
                else -> {
                    println("QR attendance failed: ${response.status} - $responseBody")
                    MarkResult.Error("Failed with status: ${response.status}")
                }
            }
        } catch (e: Exception) {
            println("Mark QR attendance error: ${e.message}")
            MarkResult.Error("Network error: ${e.message}")
        }
    }
    
    // ============= Date/Time Utilities =============
    // Simple implementations to avoid core-data dependency
    
    private fun getCurrentDate(): String {
        // Returns YYYY-MM-DD format
        val now = Clock.System.now()
        val tz = TimeZone.currentSystemDefault()
        val localDate = now.toLocalDateTime(tz).date
        return localDate.toString()
    }
    
    private fun getCurrentTimestamp(): String {
        // Returns ISO-8601 timestamp
        return Clock.System.now().toString()
    }
    
    /**
     * Reset state to allow scanning again
     */
    fun resetState() {
        _state.value = AttendanceScanState.Idle
        lastScannedCode = null
        processingLock = false
    }
    
    /**
     * Start scanning mode
     */
    fun startScanning() {
        _state.value = AttendanceScanState.Scanning
    }
}
