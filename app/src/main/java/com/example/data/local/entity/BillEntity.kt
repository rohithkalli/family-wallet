package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: Double,
    val dueDateMillis: Long,
    val recurrence: String, // "MONTHLY", "YEARLY", "ONE_TIME"
    val isPaid: Boolean,
    val category: String,
    val assignedMemberName: String = "Family"
)
