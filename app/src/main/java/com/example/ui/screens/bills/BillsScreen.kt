package com.example.ui.screens.bills

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.data.local.entity.BillEntity
import com.example.ui.components.BentoCard
import com.example.ui.components.CategoryIcon
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.formatCurrency
import com.example.ui.components.formatDate
import com.example.ui.components.formatRelativeDate
import com.example.ui.theme.FinancialExpense
import com.example.ui.theme.FinancialIncome
import com.example.ui.theme.FinancialWarning
import com.example.ui.viewmodel.WalletViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillsScreen(
    viewModel: WalletViewModel,
    onAddBill: () -> Unit,
    onEditBill: (BillEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Upcoming, 1 = All Bills

    val allBills by viewModel.bills.collectAsStateWithLifecycle()
    val upcomingBills = remember(allBills) { allBills.filter { !it.isPaid } }
    val displayedBills = if (selectedTab == 0) upcomingBills else allBills

    var billToDelete by remember { mutableStateOf<BillEntity?>(null) }

    val totalUpcoming = remember(upcomingBills) { upcomingBills.sumOf { it.amount } }

    Scaffold(
        modifier = modifier.fillMaxSize().testTag("bills_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddBill,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(bottom = 72.dp)
                    .testTag("add_bill_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Bill")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tabs: Upcoming (Unpaid) vs All Bills
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Upcoming (${upcomingBills.size})", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("tab_upcoming_bills")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("All Bills (${allBills.size})", fontWeight = FontWeight.SemiBold) },
                    modifier = Modifier.testTag("tab_all_bills")
                )
            }

            if (displayedBills.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.ReceiptLong,
                    title = if (selectedTab == 0) "No Upcoming Bills" else "No Bills Found",
                    message = if (selectedTab == 0) "All bills are marked as paid! Great financial management."
                             else "Track household utilities, subscriptions, and recurring bills.",
                    actionText = "Add First Bill",
                    onAction = onAddBill
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                        text = "UPCOMING BILLS DUE",
                                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.1.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = formatCurrency(totalUpcoming),
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                        color = FinancialWarning
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = FinancialWarning.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${upcomingBills.size} pending",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = FinancialWarning
                                        ),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Bills list
                    items(displayedBills, key = { it.id }) { bill ->
                        BillCard(
                            bill = bill,
                            onTogglePaid = { viewModel.toggleBillPaid(bill) },
                            onEdit = { onEditBill(bill) },
                            onDelete = { billToDelete = bill }
                        )
                    }
                }
            }
        }
    }

    billToDelete?.let { b ->
        ConfirmDeleteDialog(
            title = "Delete Bill?",
            message = "Are you sure you want to delete '${b.name}'?",
            onConfirm = {
                viewModel.deleteBill(b)
                billToDelete = null
            },
            onDismiss = { billToDelete = null }
        )
    }
}

@Composable
fun BillCard(
    bill: BillEntity,
    onTogglePaid: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bill_item_${bill.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (bill.isPaid) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                             else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (bill.isPaid) MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            else FinancialWarning.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Checkbox to mark paid / unpaid
            IconButton(onClick = onTogglePaid, modifier = Modifier.size(36.dp)) {
                if (bill.isPaid) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Mark Unpaid",
                        tint = FinancialIncome,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Mark Paid",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bill.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (bill.isPaid) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = bill.recurrence.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (bill.isPaid) "Paid on time" else formatRelativeDate(bill.dueDateMillis),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (bill.isPaid) FinancialIncome else FinancialWarning,
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${bill.assignedMemberName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Amount & Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatCurrency(bill.amount),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (bill.isPaid) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                )

                IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
