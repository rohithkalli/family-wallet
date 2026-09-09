package com.example.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.local.entity.BillEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.TransactionEntity
import com.example.ui.components.AddEditBillDialog
import com.example.ui.components.AddEditBudgetDialog
import com.example.ui.components.AddEditSavingsGoalDialog
import com.example.ui.components.AddEditTransactionDialog
import com.example.ui.components.AddMoneyToGoalDialog
import com.example.ui.screens.analytics.AnalyticsScreen
import com.example.ui.screens.bills.BillsScreen
import com.example.ui.screens.budget.BudgetsScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.family.FamilyScreen
import com.example.ui.screens.transactions.TransactionsScreen
import com.example.ui.viewmodel.WalletViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyWalletApp(
    viewModel: WalletViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

    val members by viewModel.members.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    // Dialog States
    var showAddTransactionDialog by remember { mutableStateOf(false) }
    var transactionDialogInitialType by remember { mutableStateOf("EXPENSE") }
    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }

    var showAddBudgetDialog by remember { mutableStateOf(false) }
    var budgetToEdit by remember { mutableStateOf<BudgetEntity?>(null) }

    var showAddGoalDialog by remember { mutableStateOf(false) }
    var goalToEdit by remember { mutableStateOf<SavingsGoalEntity?>(null) }
    var goalForDeposit by remember { mutableStateOf<SavingsGoalEntity?>(null) }

    var showAddBillDialog by remember { mutableStateOf(false) }
    var billToEdit by remember { mutableStateOf<BillEntity?>(null) }

    val currentScreen = Screen.values().find { it.route == currentRoute } ?: Screen.Dashboard

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("family_wallet_scaffold"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (currentScreen == Screen.Dashboard) "Family Wallet" else currentScreen.title,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (currentRoute == Screen.Analytics.route) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (currentRoute != Screen.Analytics.route) {
                        IconButton(
                            onClick = { navController.navigate(Screen.Analytics.route) },
                            modifier = Modifier.testTag("top_bar_analytics_button")
                        ) {
                            Icon(Icons.Default.Analytics, contentDescription = "Financial Analytics")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                Screen.bottomBarScreens.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = { Text(screen.title) },
                        modifier = Modifier.testTag("bottom_nav_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToTransactions = { navController.navigate(Screen.Transactions.route) },
                    onNavigateToBudgets = { navController.navigate(Screen.Budgets.route) },
                    onNavigateToBills = { navController.navigate(Screen.Bills.route) },
                    onNavigateToFamily = { navController.navigate(Screen.Family.route) },
                    onQuickAddExpense = {
                        transactionDialogInitialType = "EXPENSE"
                        showAddTransactionDialog = true
                    },
                    onQuickAddIncome = {
                        transactionDialogInitialType = "INCOME"
                        showAddTransactionDialog = true
                    }
                )
            }

            composable(Screen.Transactions.route) {
                TransactionsScreen(
                    viewModel = viewModel,
                    onAddTransaction = {
                        transactionDialogInitialType = "EXPENSE"
                        showAddTransactionDialog = true
                    },
                    onEditTransaction = { tx ->
                        transactionToEdit = tx
                    }
                )
            }

            composable(Screen.Budgets.route) {
                BudgetsScreen(
                    viewModel = viewModel,
                    onAddBudget = { showAddBudgetDialog = true },
                    onEditBudget = { b -> budgetToEdit = b },
                    onAddSavingsGoal = { showAddGoalDialog = true },
                    onEditSavingsGoal = { g -> goalToEdit = g },
                    onAddMoneyToGoal = { g -> goalForDeposit = g }
                )
            }

            composable(Screen.Bills.route) {
                BillsScreen(
                    viewModel = viewModel,
                    onAddBill = { showAddBillDialog = true },
                    onEditBill = { b -> billToEdit = b }
                )
            }

            composable(Screen.Family.route) {
                FamilyScreen(viewModel = viewModel)
            }

            composable(Screen.Analytics.route) {
                AnalyticsScreen(viewModel = viewModel)
            }
        }
    }

    // 1. Add / Edit Transaction Dialog
    if (showAddTransactionDialog || transactionToEdit != null) {
        AddEditTransactionDialog(
            initialTransaction = transactionToEdit,
            initialType = transactionDialogInitialType,
            members = members,
            categories = categories,
            onDismiss = {
                showAddTransactionDialog = false
                transactionToEdit = null
            },
            onSave = { amount, type, category, dateMillis, timeFormatted, memberId, memberName, notes ->
                if (transactionToEdit != null) {
                    viewModel.updateTransaction(
                        transactionToEdit!!.copy(
                            amount = amount,
                            type = type,
                            category = category,
                            dateMillis = dateMillis,
                            timeFormatted = timeFormatted,
                            memberId = memberId,
                            memberName = memberName,
                            notes = notes
                        )
                    )
                } else {
                    viewModel.addTransaction(
                        amount = amount,
                        type = type,
                        category = category,
                        dateMillis = dateMillis,
                        timeFormatted = timeFormatted,
                        memberId = memberId,
                        memberName = memberName,
                        notes = notes
                    )
                }
                showAddTransactionDialog = false
                transactionToEdit = null
            }
        )
    }

    // 2. Add / Edit Budget Dialog
    if (showAddBudgetDialog || budgetToEdit != null) {
        val budgetCategories = categories.filter { it.type == "EXPENSE" }.map { it.name }.ifEmpty {
            listOf("Groceries", "Fuel", "Shopping", "Bills", "Subscriptions", "Entertainment", "Education", "Healthcare", "Transport", "Other")
        }
        AddEditBudgetDialog(
            initialBudget = budgetToEdit,
            categories = budgetCategories,
            onDismiss = {
                showAddBudgetDialog = false
                budgetToEdit = null
            },
            onSave = { category, limit ->
                if (budgetToEdit != null) {
                    viewModel.updateBudget(budgetToEdit!!.copy(category = category, monthlyLimit = limit))
                } else {
                    viewModel.addBudget(category = category, monthlyLimit = limit)
                }
                showAddBudgetDialog = false
                budgetToEdit = null
            }
        )
    }

    // 3. Add / Edit Savings Goal Dialog
    if (showAddGoalDialog || goalToEdit != null) {
        AddEditSavingsGoalDialog(
            initialGoal = goalToEdit,
            onDismiss = {
                showAddGoalDialog = false
                goalToEdit = null
            },
            onSave = { name, targetAmount, currentSaved, targetDateMillis, notes, colorHex ->
                if (goalToEdit != null) {
                    viewModel.updateSavingsGoal(
                        goalToEdit!!.copy(
                            name = name,
                            targetAmount = targetAmount,
                            currentSaved = currentSaved,
                            targetDateMillis = targetDateMillis,
                            notes = notes,
                            colorHex = colorHex
                        )
                    )
                } else {
                    viewModel.addSavingsGoal(
                        name = name,
                        targetAmount = targetAmount,
                        currentSaved = currentSaved,
                        targetDateMillis = targetDateMillis,
                        notes = notes,
                        colorHex = colorHex
                    )
                }
                showAddGoalDialog = false
                goalToEdit = null
            }
        )
    }

    // 4. Quick Deposit to Savings Goal Dialog
    goalForDeposit?.let { goal ->
        AddMoneyToGoalDialog(
            goal = goal,
            onDismiss = { goalForDeposit = null },
            onAdd = { depositAmount ->
                viewModel.addMoneyToSavingsGoal(goal, depositAmount)
                goalForDeposit = null
            }
        )
    }

    // 5. Add / Edit Bill Dialog
    if (showAddBillDialog || billToEdit != null) {
        AddEditBillDialog(
            initialBill = billToEdit,
            members = members,
            onDismiss = {
                showAddBillDialog = false
                billToEdit = null
            },
            onSave = { name, amount, dueDateMillis, recurrence, category, assignedMemberName ->
                if (billToEdit != null) {
                    viewModel.updateBill(
                        billToEdit!!.copy(
                            name = name,
                            amount = amount,
                            dueDateMillis = dueDateMillis,
                            recurrence = recurrence,
                            category = category,
                            assignedMemberName = assignedMemberName
                        )
                    )
                } else {
                    viewModel.addBill(
                        name = name,
                        amount = amount,
                        dueDateMillis = dueDateMillis,
                        recurrence = recurrence,
                        category = category,
                        assignedMemberName = assignedMemberName
                    )
                }
                showAddBillDialog = false
                billToEdit = null
            }
        )
    }
}
