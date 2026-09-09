package com.example.domain.model

import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.FamilyMemberEntity

enum class TransactionType(val label: String) {
    ALL("All"),
    INCOME("Income"),
    EXPENSE("Expense")
}

enum class RecurrenceType(val label: String) {
    MONTHLY("Monthly"),
    YEARLY("Yearly"),
    ONE_TIME("One-time")
}

enum class TimePeriodFilter(val label: String) {
    THIS_MONTH("This Month"),
    THIS_WEEK("This Week"),
    ALL_TIME("All Time")
}

data class FamilyFinancialSummary(
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val totalSavings: Double = 0.0,
    val savingsRatePercent: Float = 0f,
    val upcomingBillsCount: Int = 0,
    val upcomingBillsTotal: Double = 0.0
)

data class BudgetProgress(
    val budget: BudgetEntity,
    val spent: Double,
    val remaining: Double,
    val percentage: Float,
    val isOverBudget: Boolean,
    val isCloseToBudget: Boolean // >= 80%
)

data class MemberFinancialSummary(
    val member: FamilyMemberEntity,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netContribution: Double = 0.0,
    val contributionPercent: Float = 0f,
    val allowanceSpent: Double = 0.0,
    val allowanceRemaining: Double = 0.0,
    val transactionCount: Int = 0
)

data class CategorySpending(
    val category: String,
    val amount: Double,
    val percentage: Float,
    val colorHex: Long,
    val iconName: String = "other"
)

data class DaySpending(
    val dayLabel: String,
    val amount: Double,
    val dateMillis: Long
)
