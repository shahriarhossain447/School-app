package com.example.data

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

object SupabaseClient {
    private const val TAG = "SupabaseClient"
    
    // Default Supabase project configuration
    var supabaseUrl: String = "https://nrvkkcmojmhoswbxjmzb.supabase.co"
    var supabaseKey: String = "sb_publishable_f-TjWgtAPy-j8-PAX9Fw3w_DCOHs9IU"

    val restUrl: String
        get() = "${supabaseUrl.removeSuffix("/")}/rest/v1"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    /**
     * Converts an arbitrary string to a deterministic valid UUID (v3)
     * Postgres uuid columns reject plain strings like "school_default",
     * so converting deterministically ensures foreign key safety and schema validity.
     */
    fun toDeterministicUuid(input: String?): String {
        if (input.isNullOrBlank()) return "00000000-0000-0000-0000-000000000001"
        return try {
            UUID.fromString(input).toString()
        } catch (e: Exception) {
            UUID.nameUUIDFromBytes(input.toByteArray()).toString()
        }
    }

    /**
     * Checks if input is already a valid UUID string, returns null if not.
     */
    fun toValidUuidOrNull(input: String?): String? {
        if (input.isNullOrBlank()) return null
        return try {
            UUID.fromString(input).toString()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Tests live connection to the Supabase endpoint.
     */
    suspend fun checkConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$restUrl/transactions?limit=1")
                .header("apikey", supabaseKey)
                .get()
                .build()
            val response = client.newCall(request).execute()
            // 200, 204 or 401 (RLS response) proves Supabase server is live & reachable
            response.isSuccessful || response.code == 401
        } catch (e: Exception) {
            Log.w(TAG, "checkConnection failed", e)
            false
        }
    }

    suspend fun signInWithPassword(identifier: String, password: String): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                if (identifier.contains("@")) {
                    put("email", identifier.trim())
                } else {
                    put("phone", identifier.trim())
                }
                put("password", password)
            }

            val request = Request.Builder()
                .url("${supabaseUrl.removeSuffix("/")}/auth/v1/token?grant_type=password")
                .header("apikey", supabaseKey)
                .header("Content-Type", "application/json")
                .post(json.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                val obj = JSONObject(body)
                val accessToken = obj.optString("access_token")
                val userObj = obj.optJSONObject("user")
                val userId = userObj?.optString("id") ?: ""
                val email = userObj?.optString("email")
                val phone = userObj?.optString("phone")

                Result.success(AuthResponse(accessToken, userId, email, phone))
            } else {
                val errorMsg = try {
                    val j = JSONObject(body)
                    val errCode = j.optString("error_code")
                    val desc = j.optString("msg", j.optString("error_description", "লগইন ব্যর্থ হয়েছে"))
                    when (errCode) {
                        "email_not_confirmed" -> "আপনার ইমেইল ভেরিফাই করা হয়নি! অনুগ্রহ করে আপনার ইনবক্স চেক করে ভেরিফিকেশন লিংকে ক্লিক করুন, অথবা নিচে 'পুনরায় ভেরিফিকেশন ইমেইল পাঠান' চাপুন।"
                        "invalid_credentials" -> "ভুল ইমেইল বা পাসওয়ার্ড। একাউন্ট তৈরি না থাকলে 'নতুন একাউন্ট' ট্যাবে গিয়ে রেজিস্ট্রেশন করুন।"
                        "over_email_send_rate_limit" -> "খুব দ্রুত রিকোয়েস্ট পাঠানো হয়েছে। অনুগ্রহ করে কিছুক্ষণ পর আবার চেষ্টা করুন।"
                        else -> desc
                    }
                } catch (e: Exception) {
                    "লগইন ব্যর্থ হয়েছে। ইন্টারনেট সংযোগ বা ক্রেডেনশিয়াল পরীক্ষা করুন।"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "signIn error", e)
            Result.failure(Exception("সার্ভারে সংযোগ করা সম্ভব হয়নি। ইন্টারনেট সংযোগ পরীক্ষা করুন।"))
        }
    }

    suspend fun signUpWithPassword(
        email: String,
        password: String,
        fullName: String,
        phone: String?
    ): Result<SignUpResult> = withContext(Dispatchers.IO) {
        try {
            val userMeta = JSONObject().apply {
                put("full_name", fullName)
                if (!phone.isNullOrBlank()) {
                    put("phone", phone)
                }
            }
            val json = JSONObject().apply {
                put("email", email.trim())
                put("password", password)
                put("data", userMeta)
            }

            val request = Request.Builder()
                .url("${supabaseUrl.removeSuffix("/")}/auth/v1/signup")
                .header("apikey", supabaseKey)
                .header("Content-Type", "application/json")
                .post(json.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                val obj = JSONObject(body)
                val accessToken = obj.optString("access_token", "").takeIf { it.isNotBlank() }
                val userId = obj.optString("id", obj.optJSONObject("user")?.optString("id") ?: "")
                val emailConfirmed = obj.optBoolean("email_confirmed", false) ||
                        !obj.optString("confirmed_at", "").isNullOrEmpty()

                Result.success(
                    SignUpResult(
                        userId = userId,
                        accessToken = accessToken,
                        email = email,
                        isConfirmed = accessToken != null || emailConfirmed
                    )
                )
            } else {
                val errorMsg = try {
                    val j = JSONObject(body)
                    val errCode = j.optString("error_code")
                    val desc = j.optString("msg", j.optString("error_description", "রেজিস্ট্রেশন ব্যর্থ হয়েছে"))
                    when (errCode) {
                        "user_already_exists" -> "এই ইমেইল দিয়ে ইতোমধ্যে একটি একাউন্ট রয়েছে। অনুগ্রহ করে লগইন করুন।"
                        "weak_password" -> "পাসওয়ার্ড অন্তত ৬ অক্ষরের হতে হবে।"
                        "over_email_send_rate_limit" -> "ইমেইল লিমিট শেষ হয়েছে। ১ মিনিট পর চেষ্টা করুন।"
                        else -> desc
                    }
                } catch (e: Exception) {
                    "রেজিস্ট্রেশন করতে সমস্যা হয়েছে।"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "signUp error", e)
            Result.failure(Exception("সার্ভারে সংযোগ করা সম্ভব হয়নি।"))
        }
    }

    suspend fun resendConfirmationEmail(email: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject().apply {
                put("type", "signup")
                put("email", email.trim())
            }
            val request = Request.Builder()
                .url("${supabaseUrl.removeSuffix("/")}/auth/v1/resend")
                .header("apikey", supabaseKey)
                .header("Content-Type", "application/json")
                .post(json.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string().orEmpty()
            if (response.isSuccessful) {
                Result.success("কনফার্মেশন ইমেইল পাঠানো হয়েছে! আপনার ইমেইল ইনবক্স চেক করুন।")
            } else {
                val errorMsg = try {
                    val j = JSONObject(body)
                    val msg = j.optString("msg", "ইমেইল পাঠানো সম্ভব হয়নি")
                    if (j.optString("error_code") == "over_email_send_rate_limit") {
                        "অনুগ্রহ করে ১ মিনিট পর পুনরায় চেষ্টা করুন (রেট লিমিট)।"
                    } else msg
                } catch (e: Exception) {
                    "ইমেইল পাঠানো সম্ভব হয়নি।"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchProfile(userId: String, token: String): Result<UserProfileEntity> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$restUrl/profiles?id=eq.$userId&select=id,full_name,phone,role,school_id")
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer $token")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                val arr = JSONArray(body)
                if (arr.length() > 0) {
                    val obj = arr.getJSONObject(0)
                    val rawSchoolId = obj.optString("school_id")
                    val profile = UserProfileEntity(
                        id = obj.optString("id"),
                        fullName = obj.optString("full_name", "User"),
                        phone = obj.optString("phone"),
                        email = null,
                        role = obj.optString("role", "Admin"),
                        schoolId = toDeterministicUuid(rawSchoolId)
                    )
                    Result.success(profile)
                } else {
                    Result.failure(Exception("Profile not found"))
                }
            } else {
                Result.failure(Exception("Failed to fetch profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncTransactionRemote(tx: TransactionEntity, token: String?): Boolean = withContext(Dispatchers.IO) {
        try {
            val safeSchoolId = toDeterministicUuid(tx.schoolId)
            val safeCreatedBy = toValidUuidOrNull(tx.createdBy)

            val json = JSONObject().apply {
                put("school_id", safeSchoolId)
                put("transaction_type", tx.transactionType)
                put("receipt_number", tx.receiptNumber)
                put("student_name", tx.studentName ?: JSONObject.NULL)
                put("roll", tx.roll ?: JSONObject.NULL)
                put("category", tx.category)
                put("amount", tx.amount)
                put("class_name", tx.className ?: JSONObject.NULL)
                put("section", tx.section ?: JSONObject.NULL)
                put("group_name", tx.groupName ?: JSONObject.NULL)
                put("description", tx.description ?: JSONObject.NULL)
                put("transaction_at", java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date(tx.transactionAt)))
                if (safeCreatedBy != null) {
                    put("created_by", safeCreatedBy)
                } else {
                    put("created_by", JSONObject.NULL)
                }
            }

            val reqBuilder = Request.Builder()
                .url("$restUrl/transactions")
                .header("apikey", supabaseKey)
                .header("Content-Type", "application/json")
                .header("Prefer", "return=representation")

            if (!token.isNullOrBlank() && token != "demo_token") {
                reqBuilder.header("Authorization", "Bearer $token")
            }

            val resp = client.newCall(reqBuilder.post(json.toString().toRequestBody(JSON_MEDIA_TYPE)).build()).execute()
            val isOk = resp.isSuccessful
            Log.d(TAG, "syncTransactionRemote code=${resp.code}, success=$isOk")
            isOk
        } catch (e: Exception) {
            Log.w(TAG, "syncTransactionRemote failed", e)
            false
        }
    }

    suspend fun fetchRemoteTransactions(token: String?): Result<List<TransactionEntity>> = withContext(Dispatchers.IO) {
        try {
            val reqBuilder = Request.Builder()
                .url("$restUrl/transactions?select=*&order=transaction_at.desc&limit=300")
                .header("apikey", supabaseKey)

            if (!token.isNullOrBlank() && token != "demo_token") {
                reqBuilder.header("Authorization", "Bearer $token")
            }

            val resp = client.newCall(reqBuilder.get().build()).execute()
            val body = resp.body?.string().orEmpty()
            if (resp.isSuccessful) {
                val array = JSONArray(body)
                val list = mutableListOf<TransactionEntity>()
                val isoFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)

                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val timeRaw = obj.optString("transaction_at")
                    val timeMs = try {
                        isoFormat.parse(timeRaw.substringBefore("Z").substringBefore("+"))?.time ?: System.currentTimeMillis()
                    } catch (e: Exception) {
                        System.currentTimeMillis()
                    }

                    list.add(
                        TransactionEntity(
                            id = 0,
                            schoolId = toDeterministicUuid(obj.optString("school_id", "00000000-0000-0000-0000-000000000001")),
                            transactionType = obj.optString("transaction_type", "payment"),
                            receiptNumber = obj.optString("receipt_number", "REC-$i"),
                            studentName = if (obj.isNull("student_name")) null else obj.optString("student_name"),
                            roll = if (obj.isNull("roll")) null else obj.optString("roll"),
                            category = obj.optString("category", "Tuition Fee"),
                            amount = obj.optDouble("amount", 0.0),
                            className = if (obj.isNull("class_name")) null else obj.optString("class_name"),
                            section = if (obj.isNull("section")) null else obj.optString("section"),
                            groupName = if (obj.isNull("group_name")) null else obj.optString("group_name"),
                            description = if (obj.isNull("description")) null else obj.optString("description"),
                            transactionAt = timeMs,
                            createdBy = if (obj.isNull("created_by")) null else obj.optString("created_by")
                        )
                    )
                }
                Result.success(list)
            } else {
                Result.failure(Exception("HTTP ${resp.code}: $body"))
            }
        } catch (e: Exception) {
            Log.w(TAG, "fetchRemoteTransactions error", e)
            Result.failure(e)
        }
    }
}

data class AuthResponse(
    val accessToken: String,
    val userId: String,
    val email: String?,
    val phone: String?
)

data class SignUpResult(
    val userId: String,
    val accessToken: String?,
    val email: String,
    val isConfirmed: Boolean
)
