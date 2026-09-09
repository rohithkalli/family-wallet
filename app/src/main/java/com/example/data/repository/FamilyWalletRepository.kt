package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.DatabaseInitializer
import com.example.data.local.entity.BillEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.FamilyMemberEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.TransactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class FamilyWalletRepository(private val database: AppDatabase) {

    private val memberDao = database.familyMemberDao()
    private val categoryDao = database.categoryDao()
    private val transactionDao = database.transactionDao()
    private val budgetDao = database.budgetDao()
    private val goalDao = database.savingsGoalDao()
    private val billDao = database.billDao()

    // Family Members
    val allMembers: Flow<List<FamilyMemberEntity>> = memberDao.getAllMembers()

    fun getMemberById(id: Long): Flow<FamilyMemberEntity?> = memberDao.getMemberById(id)

    suspend fun insertMember(member: FamilyMemberEntity): Long = withContext(Dispatchers.IO) {
        memberDao.insertMember(member)
    }

    suspend fun updateMember(member: FamilyMemberEntity) = withContext(Dispatchers.IO) {
        memberDao.updateMember(member)
    }

    suspend fun deleteMember(member: FamilyMemberEntity) = withContext(Dispatchers.IO) {
        memberDao.deleteMember(member)
    }

    // Categories
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    fun getCategoriesByType(type: String): Flow<List<CategoryEntity>> = categoryDao.getCategoriesByType(type)

    suspend fun insertCategory(category: CategoryEntity): Long = withContext(Dispatchers.IO) {
        categoryDao.insertCategory(category)
    }

    // Transactions
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    fun getRecentTransactions(limit: Int = 10): Flow<List<TransactionEntity>> =
        transactionDao.getRecentTransactions(limit)

    suspend fun insertTransaction(transaction: TransactionEntity): Long = withContext(Dispatchers.IO) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Long) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransactionById(id)
    }

    // Budgets
    val allBudgets: Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()

    suspend fun insertBudget(budget: BudgetEntity): Long = withContext(Dispatchers.IO) {
        budgetDao.insertBudget(budget)
    }

    suspend fun updateBudget(budget: BudgetEntity) = withContext(Dispatchers.IO) {
        budgetDao.updateBudget(budget)
    }

    suspend fun deleteBudget(budget: BudgetEntity) = withContext(Dispatchers.IO) {
        budgetDao.deleteBudget(budget)
    }

    suspend fun deleteBudgetById(id: Long) = withContext(Dispatchers.IO) {
        budgetDao.deleteBudgetById(id)
    }

    // Savings Goals
    val allGoals: Flow<List<SavingsGoalEntity>> = goalDao.getAllGoals()

    suspend fun insertGoal(goal: SavingsGoalEntity): Long = withContext(Dispatchers.IO) {
        goalDao.insertGoal(goal)
    }

    suspend fun updateGoal(goal: SavingsGoalEntity) = withContext(Dispatchers.IO) {
        goalDao.updateGoal(goal)
    }

    suspend fun deleteGoal(goal: SavingsGoalEntity) = withContext(Dispatchers.IO) {
        goalDao.deleteGoal(goal)
    }

    suspend fun deleteGoalById(id: Long) = withContext(Dispatchers.IO) {
        goalDao.deleteGoalById(id)
    }

    suspend fun addMoneyToGoal(id: Long, amount: Double) = withContext(Dispatchers.IO) {
        goalDao.addMoneyToGoal(id, amount)
    }

    // Bills
    val allBills: Flow<List<BillEntity>> = billDao.getAllBills()
    val upcomingBills: Flow<List<BillEntity>> = billDao.getUpcomingBills()

    suspend fun insertBill(bill: BillEntity): Long = withContext(Dispatchers.IO) {
        billDao.insertBill(bill)
    }

    suspend fun updateBill(bill: BillEntity) = withContext(Dispatchers.IO) {
        billDao.updateBill(bill)
    }

    suspend fun deleteBill(bill: BillEntity) = withContext(Dispatchers.IO) {
        billDao.deleteBill(bill)
    }

    suspend fun deleteBillById(id: Long) = withContext(Dispatchers.IO) {
        billDao.deleteBillById(id)
    }

    suspend fun setBillPaidStatus(id: Long, isPaid: Boolean) = withContext(Dispatchers.IO) {
        billDao.setBillPaidStatus(id, isPaid)
    }

    // Seed or Ensure Seeded
    suspend fun ensureDatabaseSeeded() = withContext(Dispatchers.IO) {
        DatabaseInitializer.seedDatabase(database)
    }
}
