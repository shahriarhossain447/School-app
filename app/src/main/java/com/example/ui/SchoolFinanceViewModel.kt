package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SchoolFinanceRepository
import com.example.data.SchoolSettingsEntity
import com.example.data.SupabaseClient
import com.example.data.TransactionEntity
import com.example.data.UserProfileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class FinanceSummary(
    val todayPayments: Double = 0.0,
    val todayExpenses: Double = 0.0,
    val monthIncome: Double = 0.0,
    val monthExpense: Double = 0.0,
    val monthBalance: Double = 0.0
)

data class UiState(
    val currentTab: String = "dashboard",
    val isLoggedIn: Boolean = false,
    val userProfile: UserProfileEntity? = null,
    val schoolSettings: SchoolSettingsEntity? = null,
    val transactions: List<TransactionEntity> = emptyList(),
    val summary: FinanceSummary = FinanceSummary(),
    val isDarkMode: Boolean = false,
    val language: String = "bn",
    val selectedTransaction: TransactionEntity? = null,
    val lastSavedTransaction: TransactionEntity? = null,
    val historyMonth: String = SimpleDateFormat("yyyy-MM", Locale.US).format(Date()),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val authSuccessMessage: String? = null,
    val isSupabaseConnected: Boolean = true,
    val isRefreshing: Boolean = false,
    val searchHistoryQuery: String = "",
    val supabaseUrl: String = SupabaseClient.supabaseUrl,
    val supabaseKey: String = SupabaseClient.supabaseKey
)

class SchoolFinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SchoolFinanceRepository(application)
    private var dataJob: Job? = null

    private val _uiState = MutableStateFlow(
        UiState(
            isDarkMode = repository.isDarkMode,
            language = repository.language,
            isLoggedIn = repository.currentUserId != null,
            supabaseUrl = SupabaseClient.supabaseUrl,
            supabaseKey = SupabaseClient.supabaseKey
        )
    )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        observeData()
        viewModelScope.launch(Dispatchers.IO) {
            repository.initializeDefaultDataIfNeeded()
            checkSupabaseStatus()
        }
    }

    private fun observeData() {
        dataJob?.cancel()
        val schoolId = repository.currentSchoolId

        dataJob = viewModelScope.launch(Dispatchers.Default) {
            combine(
                repository.getTransactionsFlow(schoolId),
                repository.getSchoolSettingsFlow(schoolId),
                repository.currentProfileFlow
            ) { txs, settings, profile ->
                val summary = computeSummary(txs)
                val loggedIn = profile != null || repository.currentUserId != null
                _uiState.value.copy(
                    transactions = txs,
                    schoolSettings = settings,
                    userProfile = profile,
                    summary = summary,
                    isLoggedIn = loggedIn
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    private fun computeSummary(transactions: List<TransactionEntity>): FinanceSummary {
        val todayCal = Calendar.getInstance()
        val todayYear = todayCal.get(Calendar.YEAR)
        val todayMonth = todayCal.get(Calendar.MONTH)
        val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)

        val txCal = Calendar.getInstance()
        var todayPayments = 0.0
        var todayExpenses = 0.0
        var monthIncome = 0.0
        var monthExpense = 0.0

        for (tx in transactions) {
            txCal.timeInMillis = tx.transactionAt
            val isYearMatch = txCal.get(Calendar.YEAR) == todayYear
            val isMonthMatch = isYearMatch && (txCal.get(Calendar.MONTH) == todayMonth)
            val isToday = isMonthMatch && (txCal.get(Calendar.DAY_OF_MONTH) == todayDay)

            if (tx.transactionType == "payment") {
                if (isToday) todayPayments += tx.amount
                if (isMonthMatch) monthIncome += tx.amount
            } else {
                if (isToday) todayExpenses += tx.amount
                if (isMonthMatch) monthExpense += tx.amount
            }
        }

        return FinanceSummary(
            todayPayments = todayPayments,
            todayExpenses = todayExpenses,
            monthIncome = monthIncome,
            monthExpense = monthExpense,
            monthBalance = monthIncome - monthExpense
        )
    }

    fun setTab(tab: String) {
        _uiState.value = _uiState.value.copy(currentTab = tab, selectedTransaction = null)
    }

    fun selectTab(tab: String) = setTab(tab)

    fun selectTransaction(tx: TransactionEntity?) {
        _uiState.value = _uiState.value.copy(selectedTransaction = tx)
    }

    fun dismissSuccessDialog() {
        _uiState.value = _uiState.value.copy(lastSavedTransaction = null)
    }

    fun clearLastSavedTransaction() = dismissSuccessDialog()

    fun setHistoryMonth(month: String) {
        _uiState.value = _uiState.value.copy(historyMonth = month)
    }

    fun refreshData() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isRefreshing = true, errorMessage = null)
            try {
                repository.syncRemoteTransactions()
                checkSupabaseStatus()
            } catch (e: Exception) {
                // Non-fatal
            } finally {
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchHistoryQuery = query)
    }

    fun setSearchHistoryQuery(query: String) = setSearchQuery(query)

    fun toggleDarkMode() {
        val newMode = !_uiState.value.isDarkMode
        repository.isDarkMode = newMode
        _uiState.value = _uiState.value.copy(isDarkMode = newMode)
    }

    fun setLanguage(lang: String) {
        repository.language = lang
        _uiState.value = _uiState.value.copy(language = lang)
    }

    fun savePayment(
        studentName: String,
        roll: String,
        className: String,
        section: String?,
        groupName: String?,
        category: String,
        amount: Double,
        transactionAt: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val saved = repository.addTransaction(
                    transactionType = "payment",
                    studentName = studentName,
                    roll = roll,
                    className = className,
                    section = section,
                    groupName = groupName,
                    category = category,
                    amount = amount,
                    transactionAt = transactionAt
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    lastSavedTransaction = saved,
                    currentTab = "dashboard"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "পেমেন্ট সংরক্ষণ ব্যর্থ হয়েছে"
                )
            }
        }
    }

    fun saveExpense(
        category: String,
        amount: Double,
        description: String?,
        transactionAt: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val saved = repository.addTransaction(
                    transactionType = "expense",
                    category = category,
                    amount = amount,
                    description = description,
                    transactionAt = transactionAt
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    lastSavedTransaction = saved,
                    currentTab = "dashboard"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.localizedMessage ?: "খরচ সংরক্ষণ ব্যর্থ হয়েছে"
                )
            }
        }
    }

    fun updateSchoolSettings(name: String, logoUrl: String?) {
        viewModelScope.launch {
            repository.saveSchoolSettings(name, logoUrl)
        }
    }

    fun loginWithSupabase(identifier: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, authSuccessMessage = null)
            val result = repository.loginWithSupabase(identifier, pass)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    userProfile = result.getOrNull(),
                    errorMessage = null
                )
                observeData()
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "লগইন ব্যর্থ হয়েছে"
                )
            }
        }
    }

    fun signUpWithSupabase(email: String, pass: String, fullName: String, phone: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, authSuccessMessage = null)
            val result = repository.signUpWithSupabase(email, pass, fullName, phone)
            if (result.isSuccess) {
                val data = result.getOrThrow()
                if (data.isConfirmed && !data.accessToken.isNullOrEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        errorMessage = null,
                        authSuccessMessage = "সফলভাবে একাউন্ট তৈরি ও লগইন হয়েছে!"
                    )
                    observeData()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = null,
                        authSuccessMessage = "রেজিস্ট্রেশন সফল হয়েছে! অনুগ্রহ করে আপনার ইমেইলে পাঠানো ভেরিফিকেশন লিংকে ক্লিক করে কনফার্ম করুন, তারপর লগইন করুন।"
                    )
                }
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "রেজিস্ট্রেশন ব্যর্থ হয়েছে"
                )
            }
        }
    }

    fun resendConfirmationEmail(email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.resendConfirmationEmail(email)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    authSuccessMessage = result.getOrNull()
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "ইমেইল পাঠানো যায়নি"
                )
            }
        }
    }

    fun checkSupabaseStatus() {
        viewModelScope.launch {
            val connected = repository.checkSupabaseConnection()
            _uiState.value = _uiState.value.copy(isSupabaseConnected = connected)
        }
    }

    fun updateSupabaseConfig(url: String, key: String) {
        viewModelScope.launch {
            repository.updateSupabaseConfig(url, key)
            _uiState.value = _uiState.value.copy(
                supabaseUrl = SupabaseClient.supabaseUrl,
                supabaseKey = SupabaseClient.supabaseKey
            )
            checkSupabaseStatus()
        }
    }

    fun clearAuthMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, authSuccessMessage = null)
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _uiState.value = _uiState.value.copy(
                isLoggedIn = false,
                userProfile = null,
                currentTab = "dashboard",
                errorMessage = null,
                authSuccessMessage = null
            )
            observeData()
        }
    }
}
