package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "family_members")
data class FamilyMemberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val role: String,
    val monthlyIncome: Double,
    val monthlyAllowance: Double,
    val colorHex: Long,
    val avatarName: String = ""
)
