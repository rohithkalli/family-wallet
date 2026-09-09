package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FinancialExpense
import com.example.ui.theme.FinancialIncome
import com.example.ui.theme.FinancialWarning
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun formatCurrency(amount: Double, prefix: String = "₹"): String {
    val formatter = NumberFormat.getNumberInstance(Locale("en", "IN"))
    formatter.maximumFractionDigits = 0
    formatter.minimumFractionDigits = 0
    return "$prefix${formatter.format(amount)}"
}

fun formatDate(millis: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    return sdf.format(Date(millis))
}

fun formatRelativeDate(millis: Long): String {
    val now = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val due = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val diffDays = ((due - now) / (1000 * 60 * 60 * 24)).toInt()
    return when {
        diffDays < 0 -> "Overdue by ${-diffDays}d"
        diffDays == 0 -> "Due today"
        diffDays == 1 -> "Due tomorrow"
        else -> "Due in $diffDays days"
    }
}

@Composable
fun BentoCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    borderColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun MemberAvatar(
    name: String,
    colorHex: Long,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    val initial = if (name.isNotBlank()) name.first().uppercase() else "F"
    val avatarColor = Color(colorHex)

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(avatarColor.copy(alpha = 0.2f))
            .border(1.5.dp, avatarColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.42f).sp,
                color = avatarColor
            )
        )
    }
}

@Composable
fun CategoryIcon(
    category: String,
    colorHex: Long = 0L,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier
) {
    val (icon, defaultColor) = getCategoryVisuals(category)
    val finalColor = if (colorHex != 0L) Color(colorHex) else defaultColor

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(finalColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = category,
            tint = finalColor,
            modifier = Modifier.size(size * 0.55f)
        )
    }
}

fun getCategoryVisuals(category: String): Pair<ImageVector, Color> {
    return when (category.lowercase(Locale.ROOT)) {
        "groceries" -> Icons.Default.ShoppingBasket to Color(0xFF43A047)
        "fuel" -> Icons.Default.LocalGasStation to Color(0xFFFF5722)
        "shopping" -> Icons.Default.ShoppingBag to Color(0xFFE91E63)
        "bills" -> Icons.Default.ReceiptLong to Color(0xFF3F51B5)
        "subscriptions" -> Icons.Default.Subscriptions to Color(0xFF7E57C2)
        "entertainment" -> Icons.Default.Movie to Color(0xFFFFA000)
        "education" -> Icons.Default.School to Color(0xFF00897B)
        "healthcare" -> Icons.Default.LocalHospital to Color(0xFFE53935)
        "transport" -> Icons.Default.DirectionsCar to Color(0xFF00ACC1)
        "salary" -> Icons.Default.AccountBalanceWallet to Color(0xFF2E7D32)
        "freelance" -> Icons.Default.Work to Color(0xFF1565C0)
        "investment" -> Icons.Default.AccountBalance to Color(0xFFF57F17)
        "allowance", "bonus" -> Icons.Default.CardGiftcard to Color(0xFF8E24AA)
        else -> Icons.Default.Person to Color(0xFF78909C)
    }
}

@Composable
fun CustomProgressBar(
    progress: Float,
    isWarning: Boolean = false,
    isDanger: Boolean = false,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp
) {
    val safeProgress = progress.coerceIn(0f, 1f)
    val barColor = when {
        isDanger -> FinancialExpense
        isWarning -> FinancialWarning
        else -> MaterialTheme.colorScheme.primary
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(percent = 50))
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = safeProgress)
                .height(height)
                .clip(RoundedCornerShape(percent = 50))
                .background(barColor)
        )
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    message: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onAction,
                modifier = Modifier.testTag("empty_state_action_button")
            ) {
                Text(actionText)
            }
        }
    }
}

@Composable
fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = FinancialExpense)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
