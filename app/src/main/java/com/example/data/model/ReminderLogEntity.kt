package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminder_logs")
data class ReminderLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val flatNumber: String,
    val residentName: String,
    val phone: String,
    val monthYear: String,
    val amountDue: Double,
    val message: String,
    val status: String = "Delivered"
)
