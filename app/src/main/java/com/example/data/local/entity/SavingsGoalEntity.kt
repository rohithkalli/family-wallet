package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val currentSaved: Double,
    val targetDateMillis: Long,
    val notes: String = "",
    val colorHex: Long = 0xFF4CAF50
)
