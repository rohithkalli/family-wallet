package com.example.ui.screens.analytics

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.BentoCard
import com.example.ui.components.CategoryDonutChart
import com.example.ui.components.CategoryIcon
import com.example.ui.components.CustomProgressBar
import com.example.ui.components.IncomeExpenseRatioBar
import com.example.ui.components.MemberAvatar
import com.example.ui.components.WeeklySpendingChart
import com.example.ui.components.formatCurrency
import com.example.ui.theme.FinancialExpense
import com.example.ui.theme.FinancialGoal
import com.example.ui.theme.FinancialIncome
import com.example.ui.theme.FinancialWarning
import com.example.ui.viewmodel.WalletViewModel

@Composable
fun AnalyticsScreen(
    viewModel: WalletViewModel,
    modifier: Modifier = Modifier
) {
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val weeklySpending by viewModel.weeklySpending.collectAsStateWithLifecycle()
    val categorySpending by viewModel.categorySpendingList.collectAsStateWithLifecycle()
    val memberSummaries by viewModel.memberSummaries.collectAsStateWithLifecycle()
    val budgetProgressList by viewModel.budgetProgressList.collectAsStateWithLifecycle()
    val goals by viewModel.goals.collectAsStateWithLifecycle()

    val totalFamilyExpense = summary.totalExpense

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("analytics_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title Banner
        item {
            BentoCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Analytics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Family Financial Analytics",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Live DB",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // 1. Income vs Expenses Breakdown Card
        item {
            BentoCard {
                Text(
                    text = "Income vs Expenses",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(14.dp))

                IncomeExpenseRatioBar(
                    income = summary.totalIncome,
                    expense = summary.totalExpense
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Income", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatCurrency(summary.totalIncome), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = FinancialIncome)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Expense", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatCurrency(summary.totalExpense), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = FinancialExpense)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Net Savings", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            formatCurrency(summary.totalBalance),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (summary.totalBalance >= 0) FinancialIncome else FinancialExpense
                        )
                    }
                }
            }
        }

        // 2. Weekly Spending Bar Chart
        item {
            BentoCard {
                Text(
                    text = "Weekly Spending (Last 7 Days)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(16.dp))

                WeeklySpendingChart(days = weeklySpending)
            }
        }

        // 3. Spending by Category (Donut + Full List)
        item {
            BentoCard {
                Text(
                    text = "Spending by Category",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(14.dp))

                CategoryDonutChart(
                    categories = categorySpending,
                    totalExpense = summary.totalExpense
                )

                Spacer(modifier = Modifier.height(16.dp))

                categorySpending.forEach { cat ->
                    Column(modifier = Modifier.padding(vertical = 5.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CategoryIcon(category = cat.category, colorHex = cat.colorHex, size = 28.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = cat.category,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                )
                            }
                            Text(
                                text = "${formatCurrency(cat.amount)} (${cat.percentage.toInt()}%)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        CustomProgressBar(progress = cat.percentage / 100f, height = 6.dp)
                    }
                }
            }
        }

        // 4. Member-Wise Spending Distribution
        item {
            BentoCard {
                Text(
                    text = "Member-Wise Spending Share",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(12.dp))

                memberSummaries.forEach { m ->
                    val share = if (totalFamilyExpense > 0) (m.totalExpense / totalFamilyExpense).toFloat() else 0f

                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                MemberAvatar(name = m.member.name, colorHex = m.member.colorHex, size = 30.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = m.member.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                            Text(
                                text = "${formatCurrency(m.totalExpense)} (${(share * 100).toInt()}%)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        CustomProgressBar(progress = share, height = 6.dp)
                    }
                }
            }
        }

        // 5. Budget Utilization Summary
        item {
            BentoCard {
                Text(
                    text = "Budget Utilization Summary",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(12.dp))

                budgetProgressList.forEach { bp ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(bp.budget.category, style = MaterialTheme.typography.bodyMedium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${(bp.percentage * 100).toInt()}% utilized",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        bp.isOverBudget -> FinancialExpense
                                        bp.isCloseToBudget -> FinancialWarning
                                        else -> MaterialTheme.colorScheme.primary
                                    }
                                )
                            )
                        }
                    }
                }
            }
        }

        // 6. Savings Progress
        item {
            BentoCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Savings Milestones",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = formatCurrency(summary.totalSavings),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = FinancialGoal)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                goals.forEach { g ->
                    val pct = if (g.targetAmount > 0) (g.currentSaved / g.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(g.name, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                            Text("${(pct * 100).toInt()}%", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        CustomProgressBar(progress = pct, height = 6.dp)
                    }
                }
            }
        }
    }
}
