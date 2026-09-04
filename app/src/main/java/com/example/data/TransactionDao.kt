package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE schoolId = :schoolId ORDER BY transactionAt DESC")
    fun getTransactionsBySchool(schoolId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE schoolId = :schoolId AND transactionAt >= :startTime AND transactionAt <= :endTime ORDER BY transactionAt DESC")
    fun getTransactionsBetween(schoolId: String, startTime: Long, endTime: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)

    @Query("DELETE FROM transactions WHERE schoolId = :schoolId")
    suspend fun clearAllForSchool(schoolId: String)
}

@Dao
interface SchoolDao {
    @Query("SELECT * FROM school_settings WHERE schoolId = :schoolId LIMIT 1")
    fun getSchoolSettings(schoolId: String): Flow<SchoolSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSchoolSettings(settings: SchoolSettingsEntity)

    @Query("SELECT * FROM user_profiles WHERE id = :userId LIMIT 1")
    suspend fun getUserProfile(userId: String): UserProfileEntity?

    @Query("SELECT * FROM user_profiles LIMIT 1")
    fun getCurrentProfileFlow(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfileEntity)

    @Query("DELETE FROM user_profiles")
    suspend fun clearUserProfiles()
}
