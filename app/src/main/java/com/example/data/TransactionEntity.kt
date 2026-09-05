package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["receiptNumber"], unique = true)]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val remoteId: String? = null,
    val schoolId: String,
    val transactionType: String, // "payment" or "expense"
    val receiptNumber: String,
    val studentId: String? = null,
    val studentName: String? = null,
    val roll: String? = null,
    val category: String,
    val amount: Double,
    val className: String? = null,
    val section: String? = null,
    val groupName: String? = null,
    val description: String? = null,
    val transactionAt: Long = System.currentTimeMillis(),
    val createdBy: String? = null,
    val syncStatus: Int = 0 // 0 = local, 1 = synced
)
