package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
    indices = [Index(value = ["flatId", "monthYear"], unique = true)]
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val flatId: Long,
    val monthYear: String, // format "YYYY-MM", e.g., "2026-09"
    val amountDue: Double,
    val amountPaid: Double = 0.0,
    val status: String = "PENDING", // "PAID", "PENDING", "OVERDUE"
    val paidAt: Long? = null,
    val paymentMethod: String? = null,
    val receiptNumber: String? = null,
    val lastReminderSentAt: Long? = null,
    val reminderCount: Int = 0
)
