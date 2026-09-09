package com.example.ui.screens.family

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.FamilyMemberEntity
import com.example.data.local.entity.TransactionEntity
import com.example.domain.model.MemberFinancialSummary
import com.example.ui.components.BentoCard
import com.example.ui.components.CategoryIcon
import com.example.ui.components.CustomProgressBar
import com.example.ui.components.EditMemberDialog
import com.example.ui.components.MemberAvatar
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatDate
import com.example.ui.theme.FinancialExpense
import com.example.ui.theme.FinancialIncome
import com.example.ui.viewmodel.WalletViewModel

@Composable
fun FamilyScreen(
    viewModel: WalletViewModel,
    modifier: Modifier = Modifier
) {
    val memberSummaries by viewModel.memberSummaries.collectAsStateWithLifecycle()
    val allTransactions by viewModel.transactions.collectAsStateWithLifecycle()

    var selectedMemberForDetail by remember { mutableStateOf<MemberFinancialSummary?>(null) }
    var memberToEdit by remember { mutableStateOf<FamilyMemberEntity?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("family_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Family Overview Banner
        item {
            BentoCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.FamilyRestroom,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Family of Four",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Profiles, allowances & contributions",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "4 Members",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 4 Family Member Cards
        items(memberSummaries, key = { it.member.id }) { summary ->
            FamilyMemberCard(
                summary = summary,
                onViewDetails = { selectedMemberForDetail = summary },
                onEdit = { memberToEdit = summary.member }
            )
        }
    }

    // Member Details Dialog
    selectedMemberForDetail?.let { summary ->
        val memberTransactions = remember(allTransactions, summary) {
            allTransactions.filter { it.memberId == summary.member.id }
        }

        MemberDetailDialog(
            summary = summary,
            transactions = memberTransactions,
            onDismiss = { selectedMemberForDetail = null },
            onEdit = {
                val mem = summary.member
                selectedMemberForDetail = null
                memberToEdit = mem
            }
        )
    }

    // Edit Member Dialog
    memberToEdit?.let { mem ->
        EditMemberDialog(
            member = mem,
            onDismiss = { memberToEdit = null },
            onSave = { updated ->
                viewModel.updateMember(updated)
                memberToEdit = null
            }
        )
    }
}

@Composable
fun FamilyMemberCard(
    summary: MemberFinancialSummary,
    onViewDetails: () -> Unit,
    onEdit: () -> Unit
) {
    val member = summary.member
    val allowanceProgress = if (member.monthlyAllowance > 0) {
        (summary.allowanceSpent / member.monthlyAllowance).toFloat().coerceIn(0f, 1f)
    } else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onViewDetails)
            .testTag("member_card_${member.name.lowercase()}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(member.colorHex).copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Avatar, Name, Role & Edit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MemberAvatar(name = member.name, colorHex = member.colorHex, size = 46.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = member.name,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            if (summary.contributionPercent > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(member.colorHex).copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${summary.contributionPercent.toInt()}% of income",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(member.colorHex)
                                        ),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = member.role,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Member",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2-Column Stats: Income vs Expenses
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = FinancialIncome, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Income", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatCurrency(summary.totalIncome),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (summary.totalIncome > 0) FinancialIncome else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.TrendingDown, contentDescription = null, tint = FinancialExpense, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Expenses", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = formatCurrency(summary.totalExpense),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (summary.totalExpense > 0) FinancialExpense else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Allowance Progress Bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Monthly Allowance",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "${formatCurrency(summary.allowanceSpent)} / ${formatCurrency(member.monthlyAllowance)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                CustomProgressBar(
                    progress = allowanceProgress,
                    isDanger = summary.allowanceSpent > member.monthlyAllowance && member.monthlyAllowance > 0
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${(allowanceProgress * 100).toInt()}% used",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Remaining: ${formatCurrency(summary.allowanceRemaining)}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = if (summary.allowanceRemaining > 0) MaterialTheme.colorScheme.primary else FinancialExpense
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun MemberDetailDialog(
    summary: MemberFinancialSummary,
    transactions: List<TransactionEntity>,
    onDismiss: () -> Unit,
    onEdit: () -> Unit
) {
    val member = summary.member

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MemberAvatar(name = member.name, colorHex = member.colorHex, size = 36.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(member.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text(member.role, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Financial summary pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Income", style = MaterialTheme.typography.labelSmall)
                            Text(formatCurrency(summary.totalIncome), fontWeight = FontWeight.Bold, color = FinancialIncome)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total Spent", style = MaterialTheme.typography.labelSmall)
                            Text(formatCurrency(summary.totalExpense), fontWeight = FontWeight.Bold, color = FinancialExpense)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Net", style = MaterialTheme.typography.labelSmall)
                            Text(
                                formatCurrency(summary.netContribution),
                                fontWeight = FontWeight.Bold,
                                color = if (summary.netContribution >= 0) FinancialIncome else FinancialExpense
                            )
                        }
                    }
                }

                Text(
                    text = "Spending History (${transactions.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                if (transactions.isEmpty()) {
                    Text(
                        text = "No transactions recorded for ${member.name}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(transactions) { tx ->
                            val isExpense = tx.type == "EXPENSE"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    CategoryIcon(category = tx.category, size = 30.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (tx.notes.isNotBlank()) tx.notes else tx.category,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                            maxLines = 1
                                        )
                                        Text(
                                            text = formatDate(tx.dateMillis),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    text = "${if (isExpense) "-" else "+"}${formatCurrency(tx.amount)}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isExpense) FinancialExpense else FinancialIncome
                                    )
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onEdit) {
                Text("Edit Profile")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
