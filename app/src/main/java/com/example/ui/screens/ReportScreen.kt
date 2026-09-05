package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.UiState
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.util.Translations
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    state: UiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit = {}
) {
    val lang = state.language
    val monthFormat = remember { SimpleDateFormat("yyyy-MM", Locale.US) }
    val displayMonthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.US) }

    val currentMonthKey = remember { monthFormat.format(Date()) }
    var selectedMonth by remember { mutableStateOf(currentMonthKey) }

    // Filter transactions by selected month
    val monthTransactions = remember(selectedMonth, state.transactions) {
        state.transactions.filter { tx ->
            monthFormat.format(Date(tx.transactionAt)) == selectedMonth
        }
    }

    val totalIncome = remember(monthTransactions) {
        monthTransactions.filter { it.transactionType == "payment" }.sumOf { it.amount }
    }

    val totalExpense = remember(monthTransactions) {
        monthTransactions.filter { it.transactionType == "expense" }.sumOf { it.amount }
    }

    val netBalance = totalIncome - totalExpense
    val totalVolume = totalIncome + totalExpense

    val incomePct = if (totalVolume > 0) (totalIncome / totalVolume * 100).toFloat() else 50f
    val expensePct = if (totalVolume > 0) (totalExpense / totalVolume * 100).toFloat() else 50f

    // Category aggregations
    val incomeCategories = remember(monthTransactions) {
        monthTransactions.filter { it.transactionType == "payment" }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
    }

    val expenseCategories = remember(monthTransactions) {
        monthTransactions.filter { it.transactionType == "expense" }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
    }

    // Weekly Breakdown (Week 1: days 1-7, Week 2: 8-14, Week 3: 15-21, Week 4: 22+)
    val weeklyData = remember(monthTransactions) {
        val cal = Calendar.getInstance()
        val weeksIncome = doubleArrayOf(0.0, 0.0, 0.0, 0.0)
        val weeksExpense = doubleArrayOf(0.0, 0.0, 0.0, 0.0)

        monthTransactions.forEach { tx ->
            cal.timeInMillis = tx.transactionAt
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val weekIdx = when {
                day <= 7 -> 0
                day <= 14 -> 1
                day <= 21 -> 2
                else -> 3
            }
            if (tx.transactionType == "payment") {
                weeksIncome[weekIdx] += tx.amount
            } else {
                weeksExpense[weekIdx] += tx.amount
            }
        }
        Pair(weeksIncome, weeksExpense)
    }

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        state = rememberPullToRefreshState()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("report_screen")
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))

                // Screen Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.testTag("report_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "পেছনে যান",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            Column {
                                Text(
                                    text = Translations.getString("reports_title", lang),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = Translations.getString("reports_desc", lang),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            }

            // Month Selector Bar
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = {
                            val cal = Calendar.getInstance()
                            cal.time = monthFormat.parse(selectedMonth) ?: Date()
                            cal.add(Calendar.MONTH, -1)
                            selectedMonth = monthFormat.format(cal.time)
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous Month",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            val displayDate = runCatching {
                                monthFormat.parse(selectedMonth)?.let { displayMonthFormat.format(it) }
                            }.getOrNull() ?: selectedMonth

                            Text(
                                text = displayDate,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(onClick = {
                            val cal = Calendar.getInstance()
                            cal.time = monthFormat.parse(selectedMonth) ?: Date()
                            cal.add(Calendar.MONTH, 1)
                            selectedMonth = monthFormat.format(cal.time)
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next Month",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // 3 Key Financial Metric Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Total Income
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                Text(
                                    text = Translations.getString("total_payments", lang),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = Translations.formatMoney(totalIncome),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = SuccessGreen
                            )
                        }
                    }

                    // Total Expense
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.3f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.TrendingDown, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(16.dp))
                                Text(
                                    text = Translations.getString("total_expenses", lang),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = Translations.formatMoney(totalExpense),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = ExpenseRed
                            )
                        }
                    }

                    // Net Balance
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(
                            1.dp,
                            if (netBalance >= 0) PrimaryBlue.copy(alpha = 0.3f) else ExpenseRed.copy(alpha = 0.3f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = if (netBalance >= 0) Translations.getString("net_surplus", lang)
                                else Translations.getString("net_deficit", lang),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${if (netBalance >= 0) "+" else ""}${Translations.formatMoney(netBalance)}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = if (netBalance >= 0) PrimaryBlue else ExpenseRed
                            )
                        }
                    }
                }
            }

            // Chart 1: Income vs Expense Comparative Bar Chart
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("income_expense_bar_chart"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.BarChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(
                                    text = Translations.getString("income_vs_expense", lang),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (netBalance >= 0) SuccessGreen.copy(alpha = 0.12f) else ExpenseRed.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = if (netBalance >= 0) "উদ্বৃত্ত: +${Translations.formatMoney(netBalance)}"
                                    else "ঘাটতি: ${Translations.formatMoney(netBalance)}",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (netBalance >= 0) SuccessGreen else ExpenseRed
                                )
                            }
                        }

                        // Canvas Bar Chart Component
                        IncomeExpenseBarChart(
                            income = totalIncome.toFloat(),
                            expense = totalExpense.toFloat(),
                            lang = lang
                        )
                    }
                }
            }

            // Chart 2: Income vs Expense Donut / Ratio Chart
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ratio_donut_chart"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.PieChart, contentDescription = null, tint = AccentPurple)
                            Text(
                                text = if (lang == "bn") "আয় ও ব্যয়ের অনুপাত (Donut Chart)" else "Income vs Expense Ratio",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            // Donut Canvas
                            RatioDonutChart(
                                incomePct = incomePct,
                                expensePct = expensePct,
                                netBalance = netBalance
                            )

                            // Legend Details
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                // Income Legend
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(SuccessGreen)
                                    )
                                    Column {
                                        Text(
                                            text = Translations.getString("total_payments", lang),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${String.format(Locale.US, "%.1f", incomePct)}% (${Translations.formatMoney(totalIncome)})",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = SuccessGreen
                                        )
                                    }
                                }

                                // Expense Legend
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(ExpenseRed)
                                    )
                                    Column {
                                        Text(
                                            text = Translations.getString("total_expenses", lang),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${String.format(Locale.US, "%.1f", expensePct)}% (${Translations.formatMoney(totalExpense)})",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = ExpenseRed
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Chart 3: Weekly Trend Breakdown
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("weekly_trend_chart"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Timeline, contentDescription = null, tint = PrimaryBlue)
                            Text(
                                text = Translations.getString("weekly_trend", lang),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        WeeklyTrendBarChart(
                            weeksIncome = weeklyData.first,
                            weeksExpense = weeklyData.second,
                            lang = lang
                        )
                    }
                }
            }

            // Category Breakdown Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = Translations.getString("category_breakdown", lang),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Top Income Categories
                        if (incomeCategories.isNotEmpty()) {
                            Text(
                                text = if (lang == "bn") "সর্বোচ্চ আয়ের খাতসমূহ:" else "Top Income Categories:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = SuccessGreen
                            )

                            incomeCategories.take(4).forEach { (cat, amt) ->
                                val progress = if (totalIncome > 0) (amt / totalIncome).toFloat() else 0f
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = Translations.getCategoryLabel(cat, lang),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = Translations.formatMoney(amt),
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = SuccessGreen
                                        )
                                    }
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = SuccessGreen,
                                        trackColor = SuccessGreen.copy(alpha = 0.15f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Top Expense Categories
                        if (expenseCategories.isNotEmpty()) {
                            Text(
                                text = if (lang == "bn") "সর্বোচ্চ খরচের খাতসমূহ:" else "Top Expense Drivers:",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = ExpenseRed
                            )

                            expenseCategories.take(4).forEach { (cat, amt) ->
                                val progress = if (totalExpense > 0) (amt / totalExpense).toFloat() else 0f
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = Translations.getCategoryLabel(cat, lang),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = Translations.formatMoney(amt),
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = ExpenseRed
                                        )
                                    }
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = ExpenseRed,
                                        trackColor = ExpenseRed.copy(alpha = 0.15f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}


// ----------------------------------------------------
// Chart 1: Income vs Expense Comparative Canvas Bars
// ----------------------------------------------------
@Composable
fun IncomeExpenseBarChart(
    income: Float,
    expense: Float,
    lang: String
) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(income, expense) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(700))
    }

    val maxVal = maxOf(income, expense, 1000f) * 1.15f

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val barWidth = 64.dp.toPx()
            val cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())

            // Grid guidelines
            val lineCount = 3
            val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            for (i in 1..lineCount) {
                val y = canvasHeight * (i.toFloat() / (lineCount + 1))
                drawLine(
                    color = Color.Gray.copy(alpha = 0.2f),
                    start = Offset(0f, y),
                    end = Offset(canvasWidth, y),
                    pathEffect = dashPathEffect,
                    strokeWidth = 1.dp.toPx()
                )
            }

            // Income Bar (Left)
            val incomeBarHeight = (income / maxVal) * canvasHeight * animProgress.value
            val incomeX = canvasWidth * 0.28f - barWidth / 2f
            val incomeY = canvasHeight - incomeBarHeight

            // Background bar track
            drawRoundRect(
                color = Color.LightGray.copy(alpha = 0.18f),
                topLeft = Offset(incomeX, 0f),
                size = Size(barWidth, canvasHeight),
                cornerRadius = cornerRadius
            )

            // Actual income bar
            drawRoundRect(
                color = SuccessGreen,
                topLeft = Offset(incomeX, incomeY),
                size = Size(barWidth, incomeBarHeight),
                cornerRadius = cornerRadius
            )

            // Expense Bar (Right)
            val expenseBarHeight = (expense / maxVal) * canvasHeight * animProgress.value
            val expenseX = canvasWidth * 0.72f - barWidth / 2f
            val expenseY = canvasHeight - expenseBarHeight

            // Background bar track
            drawRoundRect(
                color = Color.LightGray.copy(alpha = 0.18f),
                topLeft = Offset(expenseX, 0f),
                size = Size(barWidth, canvasHeight),
                cornerRadius = cornerRadius
            )

            // Actual expense bar
            drawRoundRect(
                color = ExpenseRed,
                topLeft = Offset(expenseX, expenseY),
                size = Size(barWidth, expenseBarHeight),
                cornerRadius = cornerRadius
            )

            // Baseline
            drawLine(
                color = Color.Gray.copy(alpha = 0.4f),
                start = Offset(0f, canvasHeight),
                end = Offset(canvasWidth, canvasHeight),
                strokeWidth = 2.dp.toPx()
            )
        }

        // Labels underneath bars
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = Translations.getString("total_payments", lang),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = SuccessGreen
                )
                Text(
                    text = Translations.formatMoney(income.toDouble()),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = Translations.getString("total_expenses", lang),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = ExpenseRed
                )
                Text(
                    text = Translations.formatMoney(expense.toDouble()),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// ----------------------------------------------------
// Chart 2: Ratio Donut Chart (Income % vs Expense %)
// ----------------------------------------------------
@Composable
fun RatioDonutChart(
    incomePct: Float,
    expensePct: Float,
    netBalance: Double
) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(incomePct, expensePct) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(800))
    }

    Box(
        modifier = Modifier.size(130.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val strokeWidth = 16.dp.toPx()
            val totalSweep = 360f * animProgress.value
            val incomeSweep = (incomePct / 100f) * totalSweep
            val expenseSweep = (expensePct / 100f) * totalSweep

            // Income Arc
            drawArc(
                color = SuccessGreen,
                startAngle = -90f,
                sweepAngle = incomeSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Expense Arc
            drawArc(
                color = ExpenseRed,
                startAngle = -90f + incomeSweep,
                sweepAngle = expenseSweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (netBalance >= 0) "লাভ" else "ঘাটতি",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${if (netBalance >= 0) "+" else ""}${Translations.formatMoney(netBalance)}",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (netBalance >= 0) SuccessGreen else ExpenseRed
            )
        }
    }
}

// ----------------------------------------------------
// Chart 3: Weekly Trend Grouped Bar Chart
// ----------------------------------------------------
@Composable
fun WeeklyTrendBarChart(
    weeksIncome: DoubleArray,
    weeksExpense: DoubleArray,
    lang: String
) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(weeksIncome, weeksExpense) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(700))
    }

    val maxVal = maxOf(
        weeksIncome.maxOrNull() ?: 1.0,
        weeksExpense.maxOrNull() ?: 1.0,
        500.0
    ).toFloat() * 1.2f

    val weekLabels = listOf(
        if (lang == "bn") "সপ্তাহ ১" else "W1",
        if (lang == "bn") "সপ্তাহ ২" else "W2",
        if (lang == "bn") "সপ্তাহ ৩" else "W3",
        if (lang == "bn") "সপ্তাহ ৪+" else "W4+"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val slotWidth = canvasWidth / 4f
            val barW = 16.dp.toPx()
            val cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())

            for (i in 0 until 4) {
                val centerX = slotWidth * i + slotWidth / 2f

                // Income bar
                val inc = weeksIncome[i].toFloat()
                val incHeight = (inc / maxVal) * canvasHeight * animProgress.value
                val incX = centerX - barW - 2.dp.toPx()
                val incY = canvasHeight - incHeight

                drawRoundRect(
                    color = SuccessGreen,
                    topLeft = Offset(incX, incY),
                    size = Size(barW, incHeight),
                    cornerRadius = cornerRadius
                )

                // Expense bar
                val exp = weeksExpense[i].toFloat()
                val expHeight = (exp / maxVal) * canvasHeight * animProgress.value
                val expX = centerX + 2.dp.toPx()
                val expY = canvasHeight - expHeight

                drawRoundRect(
                    color = ExpenseRed,
                    topLeft = Offset(expX, expY),
                    size = Size(barW, expHeight),
                    cornerRadius = cornerRadius
                )
            }

            // Baseline
            drawLine(
                color = Color.Gray.copy(alpha = 0.35f),
                start = Offset(0f, canvasHeight),
                end = Offset(canvasWidth, canvasHeight),
                strokeWidth = 1.5.dp.toPx()
            )
        }

        // Week Label Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            weekLabels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Chart mini-legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(SuccessGreen)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = Translations.getString("total_payments", lang),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(16.dp))

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(ExpenseRed)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = Translations.getString("total_expenses", lang),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
