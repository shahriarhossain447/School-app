package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class SchoolFinanceRepository(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val transactionDao = db.transactionDao()
    private val schoolDao = db.schoolDao()

    private val prefs: SharedPreferences =
        context.getSharedPreferences("school_finance_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LANG = "sf_lang"
        private const val KEY_DARK = "sf_dark"
        private const val KEY_AUTH_TOKEN = "sf_auth_token"
        private const val KEY_CURRENT_USER_ID = "sf_user_id"
        private const val KEY_CURRENT_SCHOOL_ID = "sf_school_id"
        private const val KEY_INITIALIZED = "sf_initialized_demo_v2"
        private const val KEY_SUPABASE_URL = "sf_supabase_url"
        private const val KEY_SUPABASE_KEY = "sf_supabase_key"

        // Default School ID formatted as a standard UUID for PostgreSQL
        const val DEFAULT_SCHOOL_ID = "00000000-0000-0000-0000-000000000001"
    }

    init {
        val savedUrl = prefs.getString(KEY_SUPABASE_URL, null)
        val savedKey = prefs.getString(KEY_SUPABASE_KEY, null)
        if (!savedUrl.isNullOrBlank()) {
            SupabaseClient.supabaseUrl = savedUrl
        }
        if (!savedKey.isNullOrBlank()) {
            SupabaseClient.supabaseKey = savedKey
        }
    }

    var language: String
        get() = prefs.getString(KEY_LANG, "bn") ?: "bn"
        set(value) = prefs.edit().putString(KEY_LANG, value).apply()

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK, false)
        set(value) = prefs.edit().putBoolean(KEY_DARK, value).apply()

    var authToken: String?
        get() = prefs.getString(KEY_AUTH_TOKEN, null)
        set(value) = prefs.edit().putString(KEY_AUTH_TOKEN, value).apply()

    var currentUserId: String?
        get() = prefs.getString(KEY_CURRENT_USER_ID, null)
        set(value) = prefs.edit().putString(KEY_CURRENT_USER_ID, value).apply()

    var currentSchoolId: String
        get() = prefs.getString(KEY_CURRENT_SCHOOL_ID, DEFAULT_SCHOOL_ID) ?: DEFAULT_SCHOOL_ID
        set(value) = prefs.edit().putString(KEY_CURRENT_SCHOOL_ID, value).apply()

    val currentProfileFlow: Flow<UserProfileEntity?> = schoolDao.getCurrentProfileFlow()

    fun getSchoolSettingsFlow(schoolId: String): Flow<SchoolSettingsEntity?> {
        val safeId = SupabaseClient.toDeterministicUuid(schoolId)
        return schoolDao.getSchoolSettings(safeId)
    }

    fun getTransactionsFlow(schoolId: String): Flow<List<TransactionEntity>> {
        val safeId = SupabaseClient.toDeterministicUuid(schoolId)
        return transactionDao.getTransactionsBySchool(safeId)
    }

    suspend fun initializeDefaultDataIfNeeded() = withContext(Dispatchers.IO) {
        val initialized = prefs.getBoolean(KEY_INITIALIZED, false)
        if (!initialized) {
            val safeSchoolId = DEFAULT_SCHOOL_ID

            // Create default school settings
            schoolDao.saveSchoolSettings(
                SchoolSettingsEntity(
                    schoolId = safeSchoolId,
                    schoolName = "School Finance (মডেল হাই স্কুল)",
                    logoUrl = null,
                    address = "Dhaka, Bangladesh",
                    phone = "+880 1711-000000"
                )
            )

            // Seed sample initial transactions for demonstration after user logs in
            val now = System.currentTimeMillis()
            val sampleTransactions = listOf(
                TransactionEntity(
                    schoolId = safeSchoolId,
                    transactionType = "payment",
                    receiptNumber = generateReceiptNumber("PAY"),
                    studentName = "রাফিদ আল হাসান",
                    roll = "01",
                    category = "Tuition Fee",
                    amount = 2500.0,
                    className = "Ten",
                    groupName = "Science",
                    transactionAt = now - 3600_000 * 2,
                    createdBy = null
                ),
                TransactionEntity(
                    schoolId = safeSchoolId,
                    transactionType = "payment",
                    receiptNumber = generateReceiptNumber("PAY"),
                    studentName = "সাদিয়া সুলতানা",
                    roll = "14",
                    category = "Exam Fee",
                    amount = 1200.0,
                    className = "Nine",
                    groupName = "Commerce",
                    transactionAt = now - 3600_000 * 4,
                    createdBy = null
                ),
                TransactionEntity(
                    schoolId = safeSchoolId,
                    transactionType = "expense",
                    receiptNumber = generateReceiptNumber("EXP"),
                    category = "Electricity Bill",
                    amount = 4850.0,
                    description = "অফিস ও ক্লাসরুমের বিদ্যুৎ বিল পরিশোধ",
                    transactionAt = now - 3600_000 * 6,
                    createdBy = null
                ),
                TransactionEntity(
                    schoolId = safeSchoolId,
                    transactionType = "payment",
                    receiptNumber = generateReceiptNumber("PAY"),
                    studentName = "আবরার ফাইয়াজ",
                    roll = "05",
                    category = "Monthly Fee",
                    amount = 1800.0,
                    className = "Five",
                    section = "A",
                    transactionAt = now - 3600_000 * 24,
                    createdBy = null
                ),
                TransactionEntity(
                    schoolId = safeSchoolId,
                    transactionType = "expense",
                    receiptNumber = generateReceiptNumber("EXP"),
                    category = "Salary",
                    amount = 28000.0,
                    description = "সহকারী শিক্ষক ও স্টাফদের বেতন",
                    transactionAt = now - 3600_000 * 72,
                    createdBy = null
                )
            )

            transactionDao.insertTransactions(sampleTransactions)
            prefs.edit().putBoolean(KEY_INITIALIZED, true).apply()
        }

        // Check if there is an active session
        val savedToken = authToken
        if (!savedToken.isNullOrEmpty()) {
            syncRemoteTransactions()
        }
    }

    suspend fun syncRemoteTransactions() = withContext(Dispatchers.IO) {
        try {
            val remoteRes = SupabaseClient.fetchRemoteTransactions(authToken)
            if (remoteRes.isSuccess) {
                val remoteList = remoteRes.getOrNull().orEmpty()
                if (remoteList.isNotEmpty()) {
                    transactionDao.insertTransactions(remoteList)
                }
            }
        } catch (e: Exception) {
            // Offline or transient error
        }
    }

    fun generateReceiptNumber(prefix: String): String {
        val dateStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val randomNum = Random.nextInt(100000, 999999)
        return "$prefix-$dateStr-$randomNum"
    }

    suspend fun addTransaction(
        transactionType: String,
        category: String,
        amount: Double,
        studentName: String? = null,
        roll: String? = null,
        className: String? = null,
        section: String? = null,
        groupName: String? = null,
        description: String? = null,
        transactionAt: Long = System.currentTimeMillis()
    ): TransactionEntity = withContext(Dispatchers.IO) {
        val prefix = if (transactionType == "payment") "PAY" else "EXP"
        val receipt = generateReceiptNumber(prefix)
        val safeSchoolId = SupabaseClient.toDeterministicUuid(currentSchoolId)

        val entity = TransactionEntity(
            schoolId = safeSchoolId,
            transactionType = transactionType,
            receiptNumber = receipt,
            studentName = studentName?.takeIf { it.isNotBlank() },
            roll = roll?.takeIf { it.isNotBlank() },
            category = category,
            amount = amount,
            className = className?.takeIf { it.isNotBlank() },
            section = section?.takeIf { it.isNotBlank() },
            groupName = groupName?.takeIf { it.isNotBlank() },
            description = description?.takeIf { it.isNotBlank() },
            transactionAt = transactionAt,
            createdBy = currentUserId
        )
        val id = transactionDao.insertTransaction(entity)
        val saved = entity.copy(id = id)

        // Attempt remote sync to Supabase database
        SupabaseClient.syncTransactionRemote(saved, authToken)

        saved
    }

    suspend fun getTransactionById(id: Long): TransactionEntity? = withContext(Dispatchers.IO) {
        transactionDao.getTransactionById(id)
    }

    suspend fun saveSchoolSettings(name: String, logoUrl: String?) = withContext(Dispatchers.IO) {
        val safeId = SupabaseClient.toDeterministicUuid(currentSchoolId)
        val existing = schoolDao.getSchoolSettings(safeId).firstOrNull()
        val updated = existing?.copy(
            schoolName = name,
            logoUrl = logoUrl ?: existing.logoUrl
        ) ?: SchoolSettingsEntity(
            schoolId = safeId,
            schoolName = name,
            logoUrl = logoUrl
        )
        schoolDao.saveSchoolSettings(updated)
    }

    suspend fun loginWithSupabase(identifier: String, pass: String): Result<UserProfileEntity> = withContext(Dispatchers.IO) {
        val authResult = SupabaseClient.signInWithPassword(identifier, pass)
        if (authResult.isSuccess) {
            val auth = authResult.getOrThrow()
            authToken = auth.accessToken
            currentUserId = auth.userId

            val profileRes = SupabaseClient.fetchProfile(auth.userId, auth.accessToken)
            val profile = if (profileRes.isSuccess) {
                profileRes.getOrThrow()
            } else {
                UserProfileEntity(
                    id = auth.userId,
                    fullName = if (identifier.contains("@")) identifier.substringBefore("@") else identifier,
                    email = auth.email ?: (if (identifier.contains("@")) identifier else null),
                    phone = auth.phone ?: (if (!identifier.contains("@")) identifier else null),
                    role = "Admin",
                    schoolId = SupabaseClient.toDeterministicUuid(DEFAULT_SCHOOL_ID)
                )
            }
            schoolDao.saveUserProfile(profile)
            currentSchoolId = profile.schoolId
            prefs.edit()
                .putString(KEY_AUTH_TOKEN, authToken)
                .putString(KEY_CURRENT_USER_ID, currentUserId)
                .putString(KEY_CURRENT_SCHOOL_ID, currentSchoolId)
                .apply()

            // Fetch any remote transactions
            syncRemoteTransactions()

            Result.success(profile)
        } else {
            Result.failure(authResult.exceptionOrNull() ?: Exception("লগইন ব্যর্থ হয়েছে"))
        }
    }

    suspend fun signUpWithSupabase(
        email: String,
        pass: String,
        fullName: String,
        phone: String?
    ): Result<SignUpResult> = withContext(Dispatchers.IO) {
        val signUpResult = SupabaseClient.signUpWithPassword(email, pass, fullName, phone)
        if (signUpResult.isSuccess) {
            val data = signUpResult.getOrThrow()
            if (data.isConfirmed && !data.accessToken.isNullOrEmpty()) {
                authToken = data.accessToken
                currentUserId = data.userId
                val safeSchoolId = SupabaseClient.toDeterministicUuid(DEFAULT_SCHOOL_ID)
                val profile = UserProfileEntity(
                    id = data.userId,
                    fullName = fullName,
                    email = email,
                    phone = phone,
                    role = "Admin",
                    schoolId = safeSchoolId
                )
                schoolDao.saveUserProfile(profile)
                currentSchoolId = safeSchoolId
                prefs.edit()
                    .putString(KEY_AUTH_TOKEN, authToken)
                    .putString(KEY_CURRENT_USER_ID, currentUserId)
                    .putString(KEY_CURRENT_SCHOOL_ID, currentSchoolId)
                    .apply()

                syncRemoteTransactions()
            }
        }
        signUpResult
    }

    suspend fun resendConfirmationEmail(email: String): Result<String> = withContext(Dispatchers.IO) {
        SupabaseClient.resendConfirmationEmail(email)
    }

    suspend fun updateSupabaseConfig(url: String, key: String) = withContext(Dispatchers.IO) {
        SupabaseClient.supabaseUrl = url.trim().removeSuffix("/")
        SupabaseClient.supabaseKey = key.trim()
        prefs.edit()
            .putString(KEY_SUPABASE_URL, SupabaseClient.supabaseUrl)
            .putString(KEY_SUPABASE_KEY, SupabaseClient.supabaseKey)
            .apply()
    }

    suspend fun checkSupabaseConnection(): Boolean = withContext(Dispatchers.IO) {
        SupabaseClient.checkConnection()
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        authToken = null
        currentUserId = null
        prefs.edit()
            .remove(KEY_AUTH_TOKEN)
            .remove(KEY_CURRENT_USER_ID)
            .apply()
        schoolDao.clearUserProfiles()
    }
}
