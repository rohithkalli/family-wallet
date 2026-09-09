package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.BillEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.FamilyMemberEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.repository.FamilyWalletRepository
import com.example.domain.model.BudgetProgress
import com.example.domain.model.CategorySpending
import com.example.domain.model.DaySpending
import com.example.domain.model.FamilyFinancialSummary
import com.example.domain.model.MemberFinancialSummary
import com.example.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WalletViewModel(
    private val repository: FamilyWalletRepository
) : ViewModel() {

    init {
        viewModelScope.launch {
            repository.ensureDatabaseSeeded()
        }
    }

    // Base database flows
    val members: StateFlow<List<FamilyMemberEntity>> = repository.allMembers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgets: StateFlow<List<BudgetEntity>> = repository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goals: StateFlow<List<SavingsGoalEntity>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bills: StateFlow<List<BillEntity>> = repository.allBills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filter states for transactions
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _typeFilter = MutableStateFlow(TransactionType.ALL)
    val typeFilter: StateFlow<TransactionType> = _typeFilter.asStateFlow()

    private val _memberFilterId = MutableStateFlow<Long?>(null)
    val memberFilterId: StateFlow<Long?> = _memberFilterId.asStateFlow()

    private val _categoryFilter = MutableStateFlow<String?>(null)
    val categoryFilter: StateFlow<String?> = _categoryFilter.asStateFlow()

    // Filtered Transactions
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        transactions,
        _searchQuery,
        _typeFilter,
        _memberFilterId,
        _categoryFilter
    ) { txs, query, type, memberId, cat ->
        txs.filter { tx ->
            val matchesQuery = query.isBlank() ||
                tx.notes.contains(query, ignoreCase = true) ||
                tx.category.contains(query, ignoreCase = true) ||
                tx.memberName.contains(query, ignoreCase = true)

            val matchesType = when (type) {
                TransactionType.ALL -> true
                TransactionType.INCOME -> tx.type == "INCOME"
                TransactionType.EXPENSE -> tx.type == "EXPENSE"
            }

            val matchesMember = memberId == null || tx.memberId == memberId
            val matchesCategory = cat.isNullOrBlank() || tx.category.equals(cat, ignoreCase = true)

            matchesQuery && matchesType && matchesMember && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Financial Summary: Balance = Total Income - Total Expenses
    val summary: StateFlow<FamilyFinancialSummary> = combine(
        transactions,
        goals,
        bills
    ) { txs, allGoals, allBills ->
        var totalIncome = 0.0
        var totalExpense = 0.0

        for (tx in txs) {
            if (tx.type == "INCOME") {
                totalIncome += tx.amount
            } else if (tx.type == "EXPENSE") {
                totalExpense += tx.amount
            }
        }

        val totalBalance = totalIncome - totalExpense
        val totalSavings = allGoals.sumOf { it.currentSaved }
        val savingsRate = if (totalIncome > 0) ((totalSavings / totalIncome) * 100).toFloat().coerceIn(0f, 100f) else 0f

        val unpaidBills = allBills.filter { !it.isPaid }
        val upcomingCount = unpaidBills.size
        val upcomingTotal = unpaidBills.sumOf { it.amount }

        FamilyFinancialSummary(
            totalBalance = totalBalance,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            totalSavings = totalSavings,
            savingsRatePercent = savingsRate,
            upcomingBillsCount = upcomingCount,
            upcomingBillsTotal = upcomingTotal
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FamilyFinancialSummary())

    // Budgets Progress Calculation
    val budgetProgressList: StateFlow<List<BudgetProgress>> = combine(
        budgets,
        transactions
    ) { bList, txList ->
        bList.map { b ->
            val spent = txList
                .filter { it.type == "EXPENSE" && it.category.equals(b.category, ignoreCase = true) }
                .sumOf { it.amount }
            val remaining = (b.monthlyLimit - spent).coerceAtLeast(0.0)
            val pct = if (b.monthlyLimit > 0) (spent / b.monthlyLimit).toFloat() else 0f
            val isOver = spent > b.monthlyLimit
            val isClose = spent >= (b.monthlyLimit * 0.8) && !isOver

            BudgetProgress(
                budget = b,
                spent = spent,
                remaining = remaining,
                percentage = pct,
                isOverBudget = isOver,
                isCloseToBudget = isClose
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Family Member Summaries & Contributions
    val memberSummaries: StateFlow<List<MemberFinancialSummary>> = combine(
        members,
        transactions
    ) { memList, txList ->
        val totalFamilyIncome = txList.filter { it.type == "INCOME" }.sumOf { it.amount }

        memList.map { mem ->
            val memTxs = txList.filter { it.memberId == mem.id }
            val income = memTxs.filter { it.type == "INCOME" }.sumOf { it.amount }
            val expense = memTxs.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            val net = income - expense
            val contribPct = if (totalFamilyIncome > 0) {
                ((income / totalFamilyIncome) * 100).toFloat().coerceIn(0f, 100f)
            } else 0f
            val allowanceRem = (mem.monthlyAllowance - expense).coerceAtLeast(0.0)

            MemberFinancialSummary(
                member = mem,
                totalIncome = income,
                totalExpense = expense,
                netContribution = net,
                contributionPercent = contribPct,
                allowanceSpent = expense,
                allowanceRemaining = allowanceRem,
                transactionCount = memTxs.size
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Category Spending Analytics
    val categorySpendingList: StateFlow<List<CategorySpending>> = combine(
        transactions,
        categories
    ) { txList, catList ->
        val expenses = txList.filter { it.type == "EXPENSE" }
        val totalExpense = expenses.sumOf { it.amount }
        val catMap = catList.associateBy { it.name.lowercase(Locale.ROOT) }

        val grouped = expenses.groupBy { it.category }
        grouped.map { (catName, txs) ->
            val sum = txs.sumOf { it.amount }
            val pct = if (totalExpense > 0) ((sum / totalExpense) * 100).toFloat() else 0f
            val matchCat = catMap[catName.lowercase(Locale.ROOT)]
            val color = matchCat?.colorHex ?: 0xFF607D8B
            val icon = matchCat?.iconName ?: "other"

            CategorySpending(
                category = catName,
                amount = sum,
                percentage = pct,
                colorHex = color,
                iconName = icon
            )
        }.sortedByDescending { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Weekly Spending (Last 7 days)
    val weeklySpending: StateFlow<List<DaySpending>> = transactions.combine(
        MutableStateFlow(Unit)
    ) { txList, _ ->
        val cal = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val days = mutableListOf<DaySpending>()

        // Generate last 7 days from 6 days ago to today
        for (i in 6 downTo 0) {
            val dayCal = Calendar.getInstance()
            dayCal.add(Calendar.DAY_OF_YEAR, -i)
            val dayStart = dayCal.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val dayEnd = dayCal.apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            val daySum = txList.filter {
                it.type == "EXPENSE" && it.dateMillis in dayStart..dayEnd
            }.sumOf { it.amount }

            val label = if (i == 0) "Today" else dayFormat.format(Date(dayStart))
            days.add(DaySpending(dayLabel = label, amount = daySum, dateMillis = dayStart))
        }
        days
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filter mutations
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTypeFilter(type: TransactionType) {
        _typeFilter.value = type
    }

    fun setMemberFilter(memberId: Long?) {
        _memberFilterId.value = memberId
    }

    fun setCategoryFilter(category: String?) {
        _categoryFilter.value = category
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _typeFilter.value = TransactionType.ALL
        _memberFilterId.value = null
        _categoryFilter.value = null
    }

    // Transaction Actions
    fun addTransaction(
        amount: Double,
        type: String,
        category: String,
        dateMillis: Long,
        timeFormatted: String,
        memberId: Long,
        memberName: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.insertTransaction(
                TransactionEntity(
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
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    // Budget Actions
    fun addBudget(category: String, monthlyLimit: Double) {
        viewModelScope.launch {
            repository.insertBudget(
                BudgetEntity(
                    category = category,
                    monthlyLimit = monthlyLimit
                )
            )
        }
    }

    fun updateBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            repository.updateBudget(budget)
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }

    // Savings Goal Actions
    fun addSavingsGoal(
        name: String,
        targetAmount: Double,
        currentSaved: Double,
        targetDateMillis: Long,
        notes: String,
        colorHex: Long
    ) {
        viewModelScope.launch {
            repository.insertGoal(
                SavingsGoalEntity(
                    name = name,
                    targetAmount = targetAmount,
                    currentSaved = currentSaved,
                    targetDateMillis = targetDateMillis,
                    notes = notes,
                    colorHex = colorHex
                )
            )
        }
    }

    fun updateSavingsGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            repository.updateGoal(goal)
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    fun addMoneyToGoal(goalId: Long, amount: Double) {
        viewModelScope.launch {
            repository.addMoneyToGoal(goalId, amount)
        }
    }

    fun addMoneyToSavingsGoal(goal: SavingsGoalEntity, amount: Double) {
        addMoneyToGoal(goal.id, amount)
    }

    // Bill Actions
    fun addBill(
        name: String,
        amount: Double,
        dueDateMillis: Long,
        recurrence: String,
        category: String,
        assignedMemberName: String
    ) {
        viewModelScope.launch {
            repository.insertBill(
                BillEntity(
                    name = name,
                    amount = amount,
                    dueDateMillis = dueDateMillis,
                    recurrence = recurrence,
                    isPaid = false,
                    category = category,
                    assignedMemberName = assignedMemberName
                )
            )
        }
    }

    fun updateBill(bill: BillEntity) {
        viewModelScope.launch {
            repository.updateBill(bill)
        }
    }

    fun deleteBill(bill: BillEntity) {
        viewModelScope.launch {
            repository.deleteBill(bill)
        }
    }

    fun toggleBillPaid(bill: BillEntity) {
        viewModelScope.launch {
            repository.setBillPaidStatus(bill.id, !bill.isPaid)
        }
    }

    // Member Actions
    fun updateMember(member: FamilyMemberEntity) {
        viewModelScope.launch {
            repository.updateMember(member)
        }
    }
}
