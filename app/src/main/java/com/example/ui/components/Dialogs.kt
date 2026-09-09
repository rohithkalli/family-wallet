package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.BillEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.FamilyMemberEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.TransactionEntity
import com.example.domain.model.RecurrenceType
import com.example.domain.model.TransactionType
import com.example.ui.theme.FinancialExpense
import com.example.ui.theme.FinancialIncome
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionDialog(
    initialTransaction: TransactionEntity? = null,
    initialType: String = "EXPENSE",
    members: List<FamilyMemberEntity>,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (amount: Double, type: String, category: String, dateMillis: Long, timeFormatted: String, memberId: Long, memberName: String, notes: String) -> Unit
) {
    var amountText by remember { mutableStateOf(initialTransaction?.amount?.let { if (it % 1 == 0.0) it.toLong().toString() else it.toString() } ?: "") }
    var selectedType by remember { mutableStateOf(initialTransaction?.type ?: initialType) }
    var selectedMember by remember {
        mutableStateOf(
            members.find { it.id == initialTransaction?.memberId } ?: members.firstOrNull()
        )
    }

    val availableCategories = remember(selectedType, categories) {
        val filtered = categories.filter { it.type == selectedType }
        if (filtered.isEmpty()) {
            if (selectedType == "INCOME") listOf("Salary", "Freelance", "Investment", "Bonus", "Other")
            else listOf("Groceries", "Fuel", "Shopping", "Bills", "Subscriptions", "Entertainment", "Education", "Healthcare", "Transport", "Other")
        } else {
            filtered.map { it.name }
        }
    }

    var selectedCategory by remember {
        mutableStateOf(
            initialTransaction?.category ?: availableCategories.firstOrNull() ?: "Other"
        )
    }
    var notesText by remember { mutableStateOf(initialTransaction?.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialTransaction == null) "Add Transaction" else "Edit Transaction",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Type Selector: Expense / Income
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = selectedType == "EXPENSE",
                        onClick = {
                            selectedType = "EXPENSE"
                            selectedCategory = "Groceries"
                        },
                        label = { Text("Expense", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.weight(1f).testTag("type_expense_chip")
                    )
                    FilterChip(
                        selected = selectedType == "INCOME",
                        onClick = {
                            selectedType = "INCOME"
                            selectedCategory = "Salary"
                        },
                        label = { Text("Income", fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.weight(1f).testTag("type_income_chip")
                    )
                }

                // Amount Field
                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text("Amount (₹)") },
                    placeholder = { Text("e.g. 2000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it, color = FinancialExpense) } },
                    modifier = Modifier.fillMaxWidth().testTag("transaction_amount_input"),
                    singleLine = true
                )

                // Family Member Picker
                Text(
                    text = "Family Member",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    members.forEach { mem ->
                        val isSelected = selectedMember?.id == mem.id
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) Color(mem.colorHex).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) Color(mem.colorHex) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .clickable { selectedMember = mem }
                                .testTag("member_picker_${mem.name.lowercase(Locale.ROOT)}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                MemberAvatar(name = mem.name, colorHex = mem.colorHex, size = 24.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = mem.name,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color(mem.colorHex) else MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }
                }

                // Category Selector
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    availableCategories.forEach { cat ->
                        val isSelected = selectedCategory.equals(cat, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) }
                        )
                    }
                }

                // Notes Field
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notes / Description") },
                    placeholder = { Text("e.g. Weekly organic vegetables") },
                    modifier = Modifier.fillMaxWidth().testTag("transaction_notes_input"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    if (amt == null || amt <= 0.0) {
                        errorMessage = "Please enter a valid amount greater than 0"
                        return@Button
                    }
                    val mem = selectedMember ?: members.firstOrNull()
                    if (mem == null) {
                        errorMessage = "Please select a family member"
                        return@Button
                    }

                    val date = initialTransaction?.dateMillis ?: System.currentTimeMillis()
                    val time = initialTransaction?.timeFormatted ?: SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

                    onSave(
                        amt,
                        selectedType,
                        selectedCategory,
                        date,
                        time,
                        mem.id,
                        mem.name,
                        notesText.trim()
                    )
                },
                modifier = Modifier.testTag("save_transaction_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddEditBudgetDialog(
    initialBudget: BudgetEntity? = null,
    categories: List<String>,
    onDismiss: () -> Unit,
    onSave: (category: String, monthlyLimit: Double) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(initialBudget?.category ?: categories.firstOrNull() ?: "Groceries") }
    var limitText by remember { mutableStateOf(initialBudget?.monthlyLimit?.let { if (it % 1 == 0.0) it.toLong().toString() else it.toString() } ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialBudget == null) "Set Monthly Budget" else "Edit Budget", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.take(4).forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.drop(4).take(4).forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = limitText,
                    onValueChange = {
                        limitText = it
                        errorMessage = null
                    },
                    label = { Text("Monthly Budget Limit (₹)") },
                    placeholder = { Text("e.g. 15000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it, color = FinancialExpense) } },
                    modifier = Modifier.fillMaxWidth().testTag("budget_limit_input"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = limitText.toDoubleOrNull()
                    if (limit == null || limit <= 0.0) {
                        errorMessage = "Please enter a valid budget limit"
                        return@Button
                    }
                    onSave(selectedCategory, limit)
                },
                modifier = Modifier.testTag("save_budget_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddEditSavingsGoalDialog(
    initialGoal: SavingsGoalEntity? = null,
    onDismiss: () -> Unit,
    onSave: (name: String, targetAmount: Double, currentSaved: Double, targetDateMillis: Long, notes: String, colorHex: Long) -> Unit
) {
    var nameText by remember { mutableStateOf(initialGoal?.name ?: "") }
    var targetText by remember { mutableStateOf(initialGoal?.targetAmount?.let { if (it % 1 == 0.0) it.toLong().toString() else it.toString() } ?: "") }
    var savedText by remember { mutableStateOf(initialGoal?.currentSaved?.let { if (it % 1 == 0.0) it.toLong().toString() else it.toString() } ?: "0") }
    var notesText by remember { mutableStateOf(initialGoal?.notes ?: "") }
    var monthsToTarget by remember { mutableStateOf("6") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialGoal == null) "New Savings Goal" else "Edit Savings Goal", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Goal Name") },
                    placeholder = { Text("e.g. Family Vacation (Japan)") },
                    modifier = Modifier.fillMaxWidth().testTag("goal_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = targetText,
                    onValueChange = {
                        targetText = it
                        errorMessage = null
                    },
                    label = { Text("Target Amount (₹)") },
                    placeholder = { Text("e.g. 200000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("goal_target_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = savedText,
                    onValueChange = { savedText = it },
                    label = { Text("Current Saved (₹)") },
                    placeholder = { Text("e.g. 25000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("goal_saved_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = monthsToTarget,
                    onValueChange = { monthsToTarget = it },
                    label = { Text("Target Timeframe (Months)") },
                    placeholder = { Text("e.g. 6") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notes / Purpose") },
                    placeholder = { Text("e.g. Summer holiday savings") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (errorMessage != null) {
                    Text(errorMessage!!, color = FinancialExpense, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameText.isBlank()) {
                        errorMessage = "Please enter a goal name"
                        return@Button
                    }
                    val target = targetText.toDoubleOrNull()
                    if (target == null || target <= 0.0) {
                        errorMessage = "Please enter a valid target amount"
                        return@Button
                    }
                    val saved = savedText.toDoubleOrNull() ?: 0.0
                    val months = monthsToTarget.toIntOrNull() ?: 6
                    val cal = Calendar.getInstance().apply { add(Calendar.MONTH, months) }

                    onSave(
                        nameText.trim(),
                        target,
                        saved,
                        cal.timeInMillis,
                        notesText.trim(),
                        initialGoal?.colorHex ?: 0xFF00897B
                    )
                },
                modifier = Modifier.testTag("save_goal_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddMoneyToGoalDialog(
    goal: SavingsGoalEntity,
    onDismiss: () -> Unit,
    onAdd: (amount: Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Funds to Goal", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Goal: ${goal.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Currently Saved: ${formatCurrency(goal.currentSaved)} / Target: ${formatCurrency(goal.targetAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text("Deposit Amount (₹)") },
                    placeholder = { Text("e.g. 5000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it, color = FinancialExpense) } },
                    modifier = Modifier.fillMaxWidth().testTag("add_money_input"),
                    singleLine = true
                )

                // Quick add chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1000, 2000, 5000, 10000).forEach { quickAmt ->
                        SuggestionChip(
                            onClick = { amountText = quickAmt.toString() },
                            label = { Text("+₹$quickAmt", fontSize = 11.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    if (amt == null || amt <= 0.0) {
                        errorMessage = "Please enter an amount greater than 0"
                        return@Button
                    }
                    onAdd(amt)
                },
                modifier = Modifier.testTag("confirm_deposit_button")
            ) {
                Text("Deposit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddEditBillDialog(
    initialBill: BillEntity? = null,
    members: List<FamilyMemberEntity>,
    onDismiss: () -> Unit,
    onSave: (name: String, amount: Double, dueDateMillis: Long, recurrence: String, category: String, assignedMemberName: String) -> Unit
) {
    var nameText by remember { mutableStateOf(initialBill?.name ?: "") }
    var amountText by remember { mutableStateOf(initialBill?.amount?.let { if (it % 1 == 0.0) it.toLong().toString() else it.toString() } ?: "") }
    var selectedRecurrence by remember { mutableStateOf(initialBill?.recurrence ?: "MONTHLY") }
    var selectedCategory by remember { mutableStateOf(initialBill?.category ?: "Bills") }
    var assignedMember by remember { mutableStateOf(initialBill?.assignedMemberName ?: members.firstOrNull()?.name ?: "Family") }
    var daysDueText by remember { mutableStateOf("7") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialBill == null) "Add Upcoming Bill" else "Edit Bill", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Bill Name") },
                    placeholder = { Text("e.g. Electricity Bill") },
                    modifier = Modifier.fillMaxWidth().testTag("bill_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        errorMessage = null
                    },
                    label = { Text("Amount (₹)") },
                    placeholder = { Text("e.g. 3800") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().testTag("bill_amount_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = daysDueText,
                    onValueChange = { daysDueText = it },
                    label = { Text("Due in (Days from today)") },
                    placeholder = { Text("e.g. 7") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Recurrence
                Text("Recurrence", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("MONTHLY" to "Monthly", "YEARLY" to "Yearly", "ONE_TIME" to "One-time").forEach { (rec, label) ->
                        FilterChip(
                            selected = selectedRecurrence == rec,
                            onClick = { selectedRecurrence = rec },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }

                // Assigned Member
                Text("Assigned Member", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    members.forEach { m ->
                        FilterChip(
                            selected = assignedMember == m.name,
                            onClick = { assignedMember = m.name },
                            label = { Text(m.name, fontSize = 11.sp) }
                        )
                    }
                }

                if (errorMessage != null) {
                    Text(errorMessage!!, color = FinancialExpense, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameText.isBlank()) {
                        errorMessage = "Please enter a bill name"
                        return@Button
                    }
                    val amt = amountText.toDoubleOrNull()
                    if (amt == null || amt <= 0.0) {
                        errorMessage = "Please enter a valid amount"
                        return@Button
                    }
                    val days = daysDueText.toIntOrNull() ?: 7
                    val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, days) }

                    onSave(
                        nameText.trim(),
                        amt,
                        initialBill?.dueDateMillis ?: cal.timeInMillis,
                        selectedRecurrence,
                        selectedCategory,
                        assignedMember
                    )
                },
                modifier = Modifier.testTag("save_bill_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EditMemberDialog(
    member: FamilyMemberEntity,
    onDismiss: () -> Unit,
    onSave: (FamilyMemberEntity) -> Unit
) {
    var nameText by remember { mutableStateOf(member.name) }
    var roleText by remember { mutableStateOf(member.role) }
    var incomeText by remember { mutableStateOf(if (member.monthlyIncome % 1 == 0.0) member.monthlyIncome.toLong().toString() else member.monthlyIncome.toString()) }
    var allowanceText by remember { mutableStateOf(if (member.monthlyAllowance % 1 == 0.0) member.monthlyAllowance.toLong().toString() else member.monthlyAllowance.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit ${member.name}'s Profile", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = roleText,
                    onValueChange = { roleText = it },
                    label = { Text("Role / Relationship") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = incomeText,
                    onValueChange = { incomeText = it },
                    label = { Text("Monthly Income (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = allowanceText,
                    onValueChange = { allowanceText = it },
                    label = { Text("Monthly Allowance (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val inc = incomeText.toDoubleOrNull() ?: member.monthlyIncome
                    val allw = allowanceText.toDoubleOrNull() ?: member.monthlyAllowance
                    onSave(
                        member.copy(
                            name = nameText.trim(),
                            role = roleText.trim(),
                            monthlyIncome = inc,
                            monthlyAllowance = allw
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
