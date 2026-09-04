package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TransactionEntity
import com.example.ui.UiState
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.util.Translations
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    state: UiState,
    onMonthChange: (String) -> Unit,
    onSearchChange: (String) -> Unit,
    onSelectTransaction: (TransactionEntity) -> Unit,
    onBack: () -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    val lang = state.language
    val selectedMonth = state.historyMonth

    // Calculate month navigation (previous / next month)
    val cal = remember(selectedMonth) {
        Calendar.getInstance().apply {
            try {
                val parsed = SimpleDateFormat("yyyy-MM", Locale.US).parse(selectedMonth)
                if (parsed != null) time = parsed
            } catch (e: Exception) {}
        }
    }

    val prevMonthStr = remember(selectedMonth) {
        val c = cal.clone() as Calendar
        c.add(Calendar.MONTH, -1)
        SimpleDateFormat("yyyy-MM", Locale.US).format(c.time)
    }

    val nextMonthStr = remember(selectedMonth) {
        val c = cal.clone() as Calendar
        c.add(Calendar.MONTH, 1)
        SimpleDateFormat("yyyy-MM", Locale.US).format(c.time)
    }

    // Filter transactions for this month
    val monthTransactions = remember(state.transactions, selectedMonth, state.searchHistoryQuery) {
        state.transactions.filter { tx ->
            val txMonth = SimpleDateFormat("yyyy-MM", Locale.US).format(Date(tx.transactionAt))
            val matchesMonth = txMonth == selectedMonth

            val q = state.searchHistoryQuery.trim().lowercase()
            val matchesSearch = if (q.isEmpty()) {
                true
            } else {
                (tx.studentName?.lowercase()?.contains(q) == true) ||
                (tx.roll?.lowercase()?.contains(q) == true) ||
                tx.category.lowercase().contains(q) ||
                tx.receiptNumber.lowercase().contains(q)
            }

            matchesMonth && matchesSearch
        }
    }

    val totalPayments = monthTransactions
        .filter { it.transactionType == "payment" }
        .sumOf { it.amount }

    val totalExpenses = monthTransactions
        .filter { it.transactionType == "expense" }
        .sumOf { it.amount }

    val netBalance = totalPayments - totalExpenses

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier
            .fillMaxSize()
            .testTag("history_pull_refresh")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("history_screen")
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("history_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "পেছনে যান"
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = Translations.getString("history_title", lang),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 24.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = Translations.getString("history_desc", lang),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onRefresh,
                        modifier = Modifier.testTag("history_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "রিফ্রেশ করুন",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

        // Month Selector Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onMonthChange(prevMonthStr) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Month"
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = Translations.formatMonth(selectedMonth, lang),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = { onMonthChange(nextMonthStr) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Month"
                        )
                    }
                }
            }
        }

        // Summary Boxes Grid (2x2)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HistoryStatBox(
                        modifier = Modifier.weight(1f),
                        label = Translations.getString("total_payments", lang),
                        value = Translations.formatMoney(totalPayments),
                        valueColor = SuccessGreen
                    )
                    HistoryStatBox(
                        modifier = Modifier.weight(1f),
                        label = Translations.getString("total_expenses", lang),
                        value = Translations.formatMoney(totalExpenses),
                        valueColor = ExpenseRed
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HistoryStatBox(
                        modifier = Modifier.weight(1f),
                        label = Translations.getString("total_transactions", lang),
                        value = "${monthTransactions.size}"
                    )
                    HistoryStatBox(
                        modifier = Modifier.weight(1f),
                        label = Translations.getString("net_balance", lang),
                        value = Translations.formatMoney(netBalance),
                        valueColor = if (netBalance >= 0) SuccessGreen else ExpenseRed
                    )
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = state.searchHistoryQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("history_search_input"),
                placeholder = { Text(Translations.getString("search_hint", lang), fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (state.searchHistoryQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
        }

        // Transactions List
        if (monthTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = Translations.getString("no_transactions", lang),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(monthTransactions, key = { it.id }) { tx ->
                TransactionListItem(
                    transaction = tx,
                    lang = lang,
                    onClick = { onSelectTransaction(tx) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
    }
}

@Composable
private fun HistoryStatBox(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = valueColor
            )
        }
    }
}
