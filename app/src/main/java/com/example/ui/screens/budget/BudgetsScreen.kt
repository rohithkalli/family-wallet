package com.example.ui.screens.budget

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.domain.model.BudgetProgress
import com.example.ui.components.BentoCard
import com.example.ui.components.CategoryIcon
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.CustomProgressBar
import com.example.ui.components.EmptyStateView
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatDate
import com.example.ui.theme.FinancialExpense
import com.example.ui.theme.FinancialGoal
import com.example.ui.theme.FinancialWarning
import com.example.ui.viewmodel.WalletViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    viewModel: WalletViewModel,
    onAddBudget: () -> Unit,
    onEditBudget: (BudgetEntity) -> Unit,
    onAddSavingsGoal: () -> Unit,
    onEditSavingsGoal: (SavingsGoalEntity) -> Unit,
    onAddMoneyToGoal: (SavingsGoalEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Category Budgets, 1 = Savings Goals

    val budgetProgressList by viewModel.budgetProgressList.collectAsStateWithLifecycle()
    val goals by viewModel.goals.collectAsStateWithLifecycle()

    var budgetToDelete by remember { mutableStateOf<BudgetEntity?>(null) }
    var goalToDelete by remember { mutableStateOf<SavingsGoalEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("budgets_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == 0) onAddBudget() else onAddSavingsGoal()
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(bottom = 72.dp)
                    .testTag("add_budget_or_goal_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tabs: Category Budgets vs Savings Goals
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Category Budgets (${budgetProgressList.size})", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.PieChart, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_category_budgets")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Savings Goals (${goals.size})", fontWeight = FontWeight.SemiBold) },
                    icon = { Icon(Icons.Default.Savings, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.testTag("tab_savings_goals")
                )
            }

            if (selectedTab == 0) {
                // Category Budgets Tab
                CategoryBudgetsTab(
                    budgetProgressList = budgetProgressList,
                    onAddBudget = onAddBudget,
                    onEditBudget = onEditBudget,
                    onDeleteBudget = { budgetToDelete = it }
                )
            } else {
                // Savings Goals Tab
                SavingsGoalsTab(
                    goals = goals,
                    onAddGoal = onAddSavingsGoal,
                    onEditGoal = onEditSavingsGoal,
                    onDeleteGoal = { goalToDelete = it },
                    onAddMoney = onAddMoneyToGoal
                )
            }
        }
    }

    // Confirm budget delete
    budgetToDelete?.let { b ->
        ConfirmDeleteDialog(
            title = "Delete Budget?",
            message = "Are you sure you want to remove the budget for '${b.category}'?",
            onConfirm = {
                viewModel.deleteBudget(b)
                budgetToDelete = null
            },
            onDismiss = { budgetToDelete = null }
        )
    }

    // Confirm goal delete
    goalToDelete?.let { g ->
        ConfirmDeleteDialog(
            title = "Delete Savings Goal?",
            message = "Are you sure you want to delete '${g.name}'?",
            onConfirm = {
                viewModel.deleteSavingsGoal(g)
                goalToDelete = null
            },
            onDismiss = { goalToDelete = null }
        )
    }
}

@Composable
fun CategoryBudgetsTab(
    budgetProgressList: List<BudgetProgress>,
    onAddBudget: () -> Unit,
    onEditBudget: (BudgetEntity) -> Unit,
    onDeleteBudget: (BudgetEntity) -> Unit
) {
    if (budgetProgressList.isEmpty()) {
        EmptyStateView(
            icon = Icons.Default.PieChart,
            title = "No Budgets Created",
            message = "Set up monthly category spending limits to manage family expenses",
            actionText = "Create First Budget",
            onAction = onAddBudget
        )
    } else {
        val totalLimit = budgetProgressList.sumOf { it.budget.monthlyLimit }
        val totalSpent = budgetProgressList.sumOf { it.spent }
        val overBudgetCount = budgetProgressList.count { it.isOverBudget }
        val closeToBudgetCount = budgetProgressList.count { it.isCloseToBudget }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Summary Banner
            item {
                BentoCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "MONTHLY BUDGET OVERVIEW",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.1.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${formatCurrency(totalSpent)} spent",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Total Limit",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatCurrency(totalLimit),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val overallProgress = if (totalLimit > 0) (totalSpent / totalLimit).toFloat().coerceIn(0f, 1f) else 0f
                    CustomProgressBar(
                        progress = overallProgress,
                        isDanger = totalSpent > totalLimit,
                        isWarning = totalSpent >= totalLimit * 0.8 && totalSpent <= totalLimit,
                        height = 10.dp
                    )

                    // Warning chip if budgets exceeded
                    if (overBudgetCount > 0 || closeToBudgetCount > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (overBudgetCount > 0) FinancialExpense.copy(alpha = 0.15f) else FinancialWarning.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = if (overBudgetCount > 0) FinancialExpense else FinancialWarning,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (overBudgetCount > 0) "$overBudgetCount budget(s) have exceeded monthly limit!"
                                           else "$closeToBudgetCount budget(s) are near 80% limit warning threshold.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = if (overBudgetCount > 0) FinancialExpense else FinancialWarning
                                )
                            }
                        }
                    }
                }
            }

            // Budget Items
            items(budgetProgressList, key = { it.budget.id }) { item ->
                BudgetCard(
                    progress = item,
                    onEdit = { onEditBudget(item.budget) },
                    onDelete = { onDeleteBudget(item.budget) }
                )
            }
        }
    }
}

@Composable
fun BudgetCard(
    progress: BudgetProgress,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("budget_card_${progress.budget.category.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when {
                progress.isOverBudget -> FinancialExpense.copy(alpha = 0.6f)
                progress.isCloseToBudget -> FinancialWarning.copy(alpha = 0.6f)
                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryIcon(category = progress.budget.category, size = 38.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = progress.budget.category,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Limit: ${formatCurrency(progress.budget.monthlyLimit)}/mo",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Warning Badge
                    if (progress.isOverBudget) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FinancialExpense.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Exceeded!",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = FinancialExpense
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    } else if (progress.isCloseToBudget) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = FinancialWarning.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "Near Limit",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = FinancialWarning
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            CustomProgressBar(
                progress = progress.percentage,
                isDanger = progress.isOverBudget,
                isWarning = progress.isCloseToBudget,
                height = 8.dp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Spent: ${formatCurrency(progress.spent)} (${(progress.percentage * 100).toInt()}%)",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (progress.isOverBudget) FinancialExpense else MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = if (progress.isOverBudget) "Over by ${formatCurrency(progress.spent - progress.budget.monthlyLimit)}"
                           else "Remaining: ${formatCurrency(progress.remaining)}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = if (progress.isOverBudget) FinancialExpense else MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

@Composable
fun SavingsGoalsTab(
    goals: List<SavingsGoalEntity>,
    onAddGoal: () -> Unit,
    onEditGoal: (SavingsGoalEntity) -> Unit,
    onDeleteGoal: (SavingsGoalEntity) -> Unit,
    onAddMoney: (SavingsGoalEntity) -> Unit
) {
    if (goals.isEmpty()) {
        EmptyStateView(
            icon = Icons.Default.Savings,
            title = "No Savings Goals",
            message = "Plan for family vacations, emergency funds, education, and milestones",
            actionText = "Create Savings Goal",
            onAction = onAddGoal
        )
    } else {
        val totalSaved = goals.sumOf { it.currentSaved }
        val totalTarget = goals.sumOf { it.targetAmount }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Card
            item {
                BentoCard {
                    Text(
                        text = "TOTAL FAMILY SAVINGS",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.1.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatCurrency(totalSaved),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = FinancialGoal
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Target: ${formatCurrency(totalTarget)} across ${goals.size} active goals",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Goals list
            items(goals, key = { it.id }) { goal ->
                val progress = if (goal.targetAmount > 0) (goal.currentSaved / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
                val remaining = (goal.targetAmount - goal.currentSaved).coerceAtLeast(0.0)

                Card(
                    modifier = Modifier.fillMaxWidth().testTag("savings_goal_${goal.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = goal.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                if (goal.notes.isNotBlank()) {
                                    Text(
                                        text = goal.notes,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { onEditGoal(goal) }, modifier = Modifier.size(34.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                                }
                                IconButton(onClick = { onDeleteGoal(goal) }, modifier = Modifier.size(34.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        CustomProgressBar(progress = progress, height = 8.dp)

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${formatCurrency(goal.currentSaved)} (${(progress * 100).toInt()}%)",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = FinancialGoal)
                            )
                            Text(
                                text = "Target: ${formatCurrency(goal.targetAmount)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Target Date: ${formatDate(goal.targetDateMillis)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Button(
                                onClick = { onAddMoney(goal) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("add_money_to_goal_${goal.id}")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Funds", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
