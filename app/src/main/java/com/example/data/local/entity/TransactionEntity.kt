package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: String, // "INCOME" or "EXPENSE"
    val category: String,
    val dateMillis: Long,
    val timeFormatted: String,
    val memberId: Long,
    val memberName: String,
    val notes: String
)
