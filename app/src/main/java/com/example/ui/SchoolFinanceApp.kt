package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.dialogs.SuccessReceiptDialog
import com.example.ui.dialogs.TransactionDetailDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExpenseScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.PaymentScreen
import com.example.ui.screens.ReportScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.util.Translations

@Composable
fun SchoolFinanceApp(
    viewModel: SchoolFinanceViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lang = state.language

    // Back navigation stack tracking
    var backStack by remember { mutableStateOf(listOf("dashboard")) }

    fun navigateTo(tab: String) {
        if (tab != state.currentTab) {
            backStack = backStack + tab
            viewModel.setTab(tab)
        }
    }

    fun handleBack() {
        when {
            state.selectedTransaction != null -> viewModel.selectTransaction(null)
            state.lastSavedTransaction != null -> viewModel.dismissSuccessDialog()
            backStack.size > 1 -> {
                val updatedStack = backStack.dropLast(1)
                backStack = updatedStack
                viewModel.setTab(updatedStack.last())
            }
            state.currentTab != "dashboard" -> {
                backStack = listOf("dashboard")
                viewModel.setTab("dashboard")
            }
        }
    }

    val canGoBack = state.selectedTransaction != null ||
            state.lastSavedTransaction != null ||
            backStack.size > 1 ||
            state.currentTab != "dashboard"

    BackHandler(enabled = canGoBack) {
        handleBack()
    }

    // Check if user is logged in
    if (!state.isLoggedIn) {
        LoginScreen(
            state = state,
            onLogin = { id, pass -> viewModel.loginWithSupabase(id, pass) },
            onSignUp = { email, pass, name, phone -> viewModel.signUpWithSupabase(email, pass, name, phone) },
            onResendConfirmation = { email -> viewModel.resendConfirmationEmail(email) },
            onUpdateSupabaseConfig = { url, key -> viewModel.updateSupabaseConfig(url, key) },
            onCheckServer = { viewModel.checkSupabaseStatus() },
            onClearMessages = { viewModel.clearAuthMessages() }
        )
        return
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("school_finance_main_scaffold"),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = state.currentTab == "dashboard",
                    onClick = { navigateTo("dashboard") },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = {
                        Text(
                            text = Translations.getString("dashboard", lang),
                            fontSize = 11.sp,
                            fontWeight = if (state.currentTab == "dashboard") FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )

                NavigationBarItem(
                    selected = state.currentTab == "reports",
                    onClick = { navigateTo("reports") },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null) },
                    label = {
                        Text(
                            text = Translations.getString("reports", lang),
                            fontSize = 11.sp,
                            fontWeight = if (state.currentTab == "reports") FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("nav_tab_reports")
                )

                NavigationBarItem(
                    selected = state.currentTab == "payment",
                    onClick = { navigateTo("payment") },
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = null) },
                    label = {
                        Text(
                            text = Translations.getString("payment", lang),
                            fontSize = 11.sp,
                            fontWeight = if (state.currentTab == "payment") FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("nav_tab_payment")
                )

                NavigationBarItem(
                    selected = state.currentTab == "expense",
                    onClick = { navigateTo("expense") },
                    icon = { Icon(Icons.Default.RemoveCircle, contentDescription = null) },
                    label = {
                        Text(
                            text = Translations.getString("expense", lang),
                            fontSize = 11.sp,
                            fontWeight = if (state.currentTab == "expense") FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.errorContainer,
                        selectedIconColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier.testTag("nav_tab_expense")
                )

                NavigationBarItem(
                    selected = state.currentTab == "history",
                    onClick = { navigateTo("history") },
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    label = {
                        Text(
                            text = Translations.getString("history", lang),
                            fontSize = 11.sp,
                            fontWeight = if (state.currentTab == "history") FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("nav_tab_history")
                )

                NavigationBarItem(
                    selected = state.currentTab == "settings",
                    onClick = { navigateTo("settings") },
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = {
                        Text(
                            text = Translations.getString("settings", lang),
                            fontSize = 11.sp,
                            fontWeight = if (state.currentTab == "settings") FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedIconColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = state.currentTab,
                label = "tab_transition"
            ) { tab ->
                when (tab) {
                    "dashboard" -> DashboardScreen(
                        state = state,
                        onNavigate = { navigateTo(it) },
                        onSelectTransaction = { viewModel.selectTransaction(it) },
                        onRefresh = { viewModel.refreshData() }
                    )
                    "reports" -> ReportScreen(
                        state = state,
                        onBack = { handleBack() },
                        onRefresh = { viewModel.refreshData() }
                    )
                    "payment" -> PaymentScreen(
                        state = state,
                        onSave = { name, roll, cls, sec, grp, cat, amt, time ->
                            viewModel.savePayment(name, roll, cls, sec, grp, cat, amt, time)
                        },
                        onCancel = { handleBack() }
                    )
                    "expense" -> ExpenseScreen(
                        state = state,
                        onSave = { cat, amt, desc, time ->
                            viewModel.saveExpense(cat, amt, desc, time)
                        },
                        onCancel = { handleBack() }
                    )
                    "history" -> HistoryScreen(
                        state = state,
                        onMonthChange = { viewModel.setHistoryMonth(it) },
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onSelectTransaction = { viewModel.selectTransaction(it) },
                        onBack = { handleBack() },
                        onRefresh = { viewModel.refreshData() }
                    )
                    "settings" -> SettingsScreen(
                        state = state,
                        onSaveSchoolName = { viewModel.updateSchoolSettings(it, null) },
                        onToggleDarkMode = { viewModel.toggleDarkMode() },
                        onLanguageChange = { viewModel.setLanguage(it) },
                        onLogout = { viewModel.logout() },
                        onBack = { handleBack() }
                    )
                }
            }

            // Transaction Detail Dialog
            state.selectedTransaction?.let { tx ->
                TransactionDetailDialog(
                    transaction = tx,
                    lang = lang,
                    onDismiss = { viewModel.selectTransaction(null) }
                )
            }

            // Success Receipt Dialog
            state.lastSavedTransaction?.let { tx ->
                SuccessReceiptDialog(
                    transaction = tx,
                    lang = lang,
                    onDismiss = { viewModel.dismissSuccessDialog() }
                )
            }
        }
    }
}
