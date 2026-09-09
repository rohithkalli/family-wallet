package com.example.ui.screens.transactions

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.data.local.entity.TransactionEntity
import com.example.domain.model.TransactionType
import com.example.ui.components.CategoryIcon
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MemberAvatar
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatDate
import com.example.ui.theme.FinancialExpense
import com.example.ui.theme.FinancialIncome
import com.example.ui.viewmodel.WalletViewModel

@Composable
fun TransactionsScreen(
    viewModel: WalletViewModel,
    onAddTransaction: () -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val transactions by viewModel.filteredTransactions.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedType by viewModel.typeFilter.collectAsStateWithLifecycle()
    val selectedMemberId by viewModel.memberFilterId.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.categoryFilter.collectAsStateWithLifecycle()

    val members by viewModel.members.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()

    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("transactions_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransaction,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(bottom = 72.dp)
                    .testTag("add_transaction_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search transactions, notes, members...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("search_transactions_input"),
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            // Filter Row 1: Type (All, Expense, Income)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedType == TransactionType.ALL,
                        onClick = { viewModel.setTypeFilter(TransactionType.ALL) },
                        label = { Text("All Types") },
                        modifier = Modifier.testTag("filter_all_types")
                    )
                }
                item {
                    FilterChip(
                        selected = selectedType == TransactionType.EXPENSE,
                        onClick = { viewModel.setTypeFilter(TransactionType.EXPENSE) },
                        label = { Text("Expenses") },
                        modifier = Modifier.testTag("filter_expenses_only")
                    )
                }
                item {
                    FilterChip(
                        selected = selectedType == TransactionType.INCOME,
                        onClick = { viewModel.setTypeFilter(TransactionType.INCOME) },
                        label = { Text("Income") },
                        modifier = Modifier.testTag("filter_income_only")
                    )
                }

                // Member Chips
                items(members) { mem ->
                    val isSelected = selectedMemberId == mem.id
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) viewModel.setMemberFilter(null)
                            else viewModel.setMemberFilter(mem.id)
                        },
                        label = { Text(mem.name) },
                        leadingIcon = {
                            MemberAvatar(name = mem.name, colorHex = mem.colorHex, size = 18.dp)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Transaction List or Empty State
            if (transactions.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.ReceiptLong,
                    title = if (searchQuery.isNotBlank()) "No Matching Transactions" else "No Transactions Recorded",
                    message = if (searchQuery.isNotBlank()) "Try changing your search or filter criteria" else "Record an expense or income to track family finances",
                    actionText = if (searchQuery.isNotBlank()) "Clear Filters" else "Add First Transaction",
                    onAction = {
                        if (searchQuery.isNotBlank() || selectedMemberId != null || selectedType != TransactionType.ALL) {
                            viewModel.clearFilters()
                        } else {
                            onAddTransaction()
                        }
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(transactions, key = { it.id }) { tx ->
                        TransactionCard(
                            transaction = tx,
                            onEdit = { onEditTransaction(tx) },
                            onDelete = { transactionToDelete = tx }
                        )
                    }
                }
            }
        }
    }

    // Confirmation dialog for deletion
    transactionToDelete?.let { tx ->
        ConfirmDeleteDialog(
            title = "Delete Transaction?",
            message = "Are you sure you want to delete this ${formatCurrency(tx.amount)} transaction for '${tx.category}'? This will update all balances and budgets.",
            onConfirm = {
                viewModel.deleteTransaction(tx)
                transactionToDelete = null
            },
            onDismiss = { transactionToDelete = null }
        )
    }
}

@Composable
fun TransactionCard(
    transaction: TransactionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isExpense = transaction.type == "EXPENSE"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
            .testTag("transaction_item_${transaction.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Category Icon & Details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                CategoryIcon(category = transaction.category, size = 42.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (transaction.notes.isNotBlank()) transaction.notes else transaction.category,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = transaction.memberName,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${formatDate(transaction.dateMillis)} • ${transaction.timeFormatted}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Amount & Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${if (isExpense) "-" else "+"}${formatCurrency(transaction.amount)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isExpense) FinancialExpense else FinancialIncome
                        )
                    )
                    Text(
                        text = transaction.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Transaction",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Transaction",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
