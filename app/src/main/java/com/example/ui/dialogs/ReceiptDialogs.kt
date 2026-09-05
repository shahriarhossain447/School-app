package com.example.ui.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.TransactionEntity
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenContainer
import com.example.ui.util.Translations

@Composable
fun SuccessReceiptDialog(
    transaction: TransactionEntity,
    lang: String,
    onDismiss: () -> Unit
) {
    val isPayment = transaction.transactionType == "payment"
    val titleText = if (isPayment) {
        Translations.getString("saved_payment", lang)
    } else {
        Translations.getString("saved_expense", lang)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("success_receipt_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animated green checkmark
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(SuccessGreenContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = SuccessGreen,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Receipt Card Container
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ReceiptRow(
                            label = Translations.getString("receipt_number", lang),
                            value = transaction.receiptNumber
                        )

                        if (!transaction.studentName.isNullOrBlank()) {
                            ReceiptRow(
                                label = Translations.getString("student_name", lang),
                                value = transaction.studentName
                            )
                        }

                        if (!transaction.roll.isNullOrBlank()) {
                            ReceiptRow(
                                label = Translations.getString("roll", lang),
                                value = transaction.roll
                            )
                        }

                        ReceiptRow(
                            label = Translations.getString("category", lang),
                            value = Translations.getCategoryLabel(transaction.category, lang)
                        )

                        ReceiptRow(
                            label = Translations.getString("amount", lang),
                            value = Translations.formatMoney(transaction.amount),
                            isBold = true,
                            valueColor = if (isPayment) SuccessGreen else ExpenseRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("done_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = Translations.getString("done", lang),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionDetailDialog(
    transaction: TransactionEntity,
    lang: String,
    onDismiss: () -> Unit
) {
    val isPayment = transaction.transactionType == "payment"

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("transaction_detail_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = Translations.getString("transaction_details", lang),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                DetailItem(
                    label = Translations.getString("transaction_type", lang),
                    value = if (isPayment) Translations.getString("payment", lang) else Translations.getString("expense", lang)
                )

                DetailItem(
                    label = Translations.getString("receipt_number", lang),
                    value = transaction.receiptNumber
                )

                if (!transaction.studentName.isNullOrBlank()) {
                    DetailItem(
                        label = Translations.getString("student_name", lang),
                        value = transaction.studentName
                    )
                }

                if (!transaction.roll.isNullOrBlank()) {
                    DetailItem(
                        label = Translations.getString("roll", lang),
                        value = transaction.roll
                    )
                }

                DetailItem(
                    label = Translations.getString("category", lang),
                    value = Translations.getCategoryLabel(transaction.category, lang)
                )

                if (!transaction.className.isNullOrBlank()) {
                    val classDisplay = Translations.classOptions.find { it.value == transaction.className }?.label ?: transaction.className
                    DetailItem(
                        label = Translations.getString("class", lang),
                        value = classDisplay
                    )
                }

                if (!transaction.section.isNullOrBlank()) {
                    DetailItem(
                        label = Translations.getString("section", lang),
                        value = transaction.section
                    )
                }

                if (!transaction.groupName.isNullOrBlank()) {
                    DetailItem(
                        label = Translations.getString("group", lang),
                        value = Translations.getGroupLabel(transaction.groupName, lang)
                    )
                }

                if (!transaction.description.isNullOrBlank()) {
                    DetailItem(
                        label = Translations.getString("description", lang),
                        value = transaction.description
                    )
                }

                DetailItem(
                    label = Translations.getString("amount", lang),
                    value = "${if (isPayment) "+" else "-"}${Translations.formatMoney(transaction.amount)}",
                    valueColor = if (isPayment) SuccessGreen else ExpenseRed,
                    isBold = true
                )

                DetailItem(
                    label = Translations.getString("date_time", lang),
                    value = Translations.formatDate(transaction.transactionAt, lang)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = Translations.getString("done", lang),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
            color = valueColor
        )
    }
}

@Composable
private fun DetailItem(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    isBold: Boolean = false
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
                color = valueColor
            )
        }
        HorizontalDivider(
            thickness = 0.8.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    }
}
