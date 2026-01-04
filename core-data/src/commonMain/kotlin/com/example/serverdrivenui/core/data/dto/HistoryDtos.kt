package com.example.serverdrivenui.core.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Payment History Data Transfer Object
 */
@Serializable
data class PaymentHistoryDto(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    val amount: String = "",
    @SerialName("payment_date") val paymentDate: String = "",
    val method: String = "UPI",
    val status: String = "completed",
    @SerialName("created_at") val createdAt: String? = null
)

/**
 * Membership History Data Transfer Object
 */
@Serializable
data class MembershipHistoryDto(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("plan_name") val planName: String = "",
    @SerialName("start_date") val startDate: String = "",
    @SerialName("end_date") val endDate: String = "",
    val status: String = "completed",
    @SerialName("created_at") val createdAt: String? = null
)

/**
 * Attendance Data Transfer Object
 */
@Serializable
data class AttendanceDto(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    val date: String = "",
    val status: String = "present",
    @SerialName("created_at") val createdAt: String? = null
)
