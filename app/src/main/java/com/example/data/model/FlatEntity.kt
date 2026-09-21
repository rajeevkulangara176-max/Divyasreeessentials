package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flats")
data class FlatEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val flatNumber: String,
    val block: String,
    val floor: Int,
    val residentName: String,
    val residentType: String = "Owner", // "Owner" or "Tenant"
    val phone: String,
    val email: String,
    val monthlyFee: Double = 150.0,
    val active: Boolean = true
)
