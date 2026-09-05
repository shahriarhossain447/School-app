package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "school_settings")
data class SchoolSettingsEntity(
    @PrimaryKey
    val schoolId: String,
    val schoolName: String = "School Finance",
    val logoUrl: String? = null,
    val address: String? = null,
    val phone: String? = null
)

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey
    val id: String,
    val fullName: String,
    val phone: String? = null,
    val email: String? = null,
    val role: String = "Admin", // admin, principal, accountant
    val schoolId: String
)
