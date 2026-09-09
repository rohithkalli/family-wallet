package com.example.data.local

import com.example.data.local.entity.BillEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.FamilyMemberEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.TransactionEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DatabaseInitializer {

    suspend fun seedDatabase(database: AppDatabase) {
        val memberDao = database.familyMemberDao()
        val categoryDao = database.categoryDao()
        val budgetDao = database.budgetDao()
        val goalDao = database.savingsGoalDao()
        val billDao = database.billDao()
        val transactionDao = database.transactionDao()

        // Check if already seeded
        if (memberDao.getMemberCount() > 0) return

        // 1. Four Family Members
        val dad = FamilyMemberEntity(
            name = "John",
            role = "Father / Primary Earner",
            monthlyIncome = 85000.0,
            monthlyAllowance = 12000.0,
            colorHex = 0xFF1E88E5,
            avatarName = "dad"
        )
        val mom = FamilyMemberEntity(
            name = "Sarah",
            role = "Mother / Architect",
            monthlyIncome = 65000.0,
            monthlyAllowance = 12000.0,
            colorHex = 0xFFE91E63,
            avatarName = "mom"
        )
        val son = FamilyMemberEntity(
            name = "Leo",
            role = "Son / High School",
            monthlyIncome = 0.0,
            monthlyAllowance = 4000.0,
            colorHex = 0xFFFF9800,
            avatarName = "son"
        )
        val daughter = FamilyMemberEntity(
            name = "Maya",
            role = "Daughter / Middle School",
            monthlyIncome = 0.0,
            monthlyAllowance = 2500.0,
            colorHex = 0xFF9C27B0,
            avatarName = "daughter"
        )

        val dadId = memberDao.insertMember(dad)
        val momId = memberDao.insertMember(mom)
        val sonId = memberDao.insertMember(son)
        val daughterId = memberDao.insertMember(daughter)

        // 2. Categories
        val expenseCategories = listOf(
            CategoryEntity(name = "Groceries", type = "EXPENSE", iconName = "groceries", colorHex = 0xFF43A047),
            CategoryEntity(name = "Fuel", type = "EXPENSE", iconName = "fuel", colorHex = 0xFFFF5722),
            CategoryEntity(name = "Shopping", type = "EXPENSE", iconName = "shopping", colorHex = 0xFFE91E63),
            CategoryEntity(name = "Bills", type = "EXPENSE", iconName = "bills", colorHex = 0xFF3F51B5),
            CategoryEntity(name = "Subscriptions", type = "EXPENSE", iconName = "subscriptions", colorHex = 0xFF7E57C2),
            CategoryEntity(name = "Entertainment", type = "EXPENSE", iconName = "entertainment", colorHex = 0xFFFFA000),
            CategoryEntity(name = "Education", type = "EXPENSE", iconName = "education", colorHex = 0xFF00897B),
            CategoryEntity(name = "Healthcare", type = "EXPENSE", iconName = "healthcare", colorHex = 0xFFE53935),
            CategoryEntity(name = "Transport", type = "EXPENSE", iconName = "transport", colorHex = 0xFF00ACC1),
            CategoryEntity(name = "Other", type = "EXPENSE", iconName = "other", colorHex = 0xFF78909C)
        )
        val incomeCategories = listOf(
            CategoryEntity(name = "Salary", type = "INCOME", iconName = "salary", colorHex = 0xFF2E7D32),
            CategoryEntity(name = "Freelance", type = "INCOME", iconName = "freelance", colorHex = 0xFF1565C0),
            CategoryEntity(name = "Investment", type = "INCOME", iconName = "investment", colorHex = 0xFFF57F17),
            CategoryEntity(name = "Allowance", type = "INCOME", iconName = "allowance", colorHex = 0xFF8E24AA),
            CategoryEntity(name = "Bonus", type = "INCOME", iconName = "bonus", colorHex = 0xFFFFB300)
        )
        categoryDao.insertCategories(expenseCategories + incomeCategories)

        // 3. Budgets
        val budgets = listOf(
            BudgetEntity(category = "Groceries", monthlyLimit = 22000.0),
            BudgetEntity(category = "Fuel", monthlyLimit = 8000.0),
            BudgetEntity(category = "Shopping", monthlyLimit = 12000.0),
            BudgetEntity(category = "Subscriptions", monthlyLimit = 3500.0),
            BudgetEntity(category = "Bills", monthlyLimit = 15000.0),
            BudgetEntity(category = "Entertainment", monthlyLimit = 7000.0)
        )
        budgetDao.insertBudgets(budgets)

        // 4. Savings Goals
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        
        calendar.timeInMillis = now
        calendar.add(Calendar.MONTH, 6)
        val emergencyGoalDate = calendar.timeInMillis

        calendar.timeInMillis = now
        calendar.add(Calendar.MONTH, 4)
        val vacationGoalDate = calendar.timeInMillis

        calendar.timeInMillis = now
        calendar.add(Calendar.MONTH, 9)
        val renovationGoalDate = calendar.timeInMillis

        val goals = listOf(
            SavingsGoalEntity(
                name = "Emergency Fund",
                targetAmount = 300000.0,
                currentSaved = 185000.0,
                targetDateMillis = emergencyGoalDate,
                notes = "6 months living expenses safety net",
                colorHex = 0xFF00897B
            ),
            SavingsGoalEntity(
                name = "Family Vacation (Japan)",
                targetAmount = 200000.0,
                currentSaved = 115000.0,
                targetDateMillis = vacationGoalDate,
                notes = "Cherry blossom family holiday",
                colorHex = 0xFF1E88E5
            ),
            SavingsGoalEntity(
                name = "Home Renovation",
                targetAmount = 150000.0,
                currentSaved = 45000.0,
                targetDateMillis = renovationGoalDate,
                notes = "Kitchen upgrade & solar panels",
                colorHex = 0xFFFB8C00
            )
        )
        goalDao.insertGoals(goals)

        // 5. Bills
        val billDates = listOf(
            daysFromNow(4),
            daysFromNow(8),
            daysFromNow(12),
            daysFromNow(17),
            daysFromNow(22)
        )

        val bills = listOf(
            BillEntity(
                name = "Electricity & Water Bill",
                amount = 3800.0,
                dueDateMillis = billDates[0],
                recurrence = "MONTHLY",
                isPaid = false,
                category = "Bills",
                assignedMemberName = "John"
            ),
            BillEntity(
                name = "High-Speed Fiber Internet",
                amount = 1299.0,
                dueDateMillis = billDates[1],
                recurrence = "MONTHLY",
                isPaid = false,
                category = "Bills",
                assignedMemberName = "Sarah"
            ),
            BillEntity(
                name = "Netflix & Spotify Family",
                amount = 1099.0,
                dueDateMillis = billDates[2],
                recurrence = "MONTHLY",
                isPaid = false,
                category = "Subscriptions",
                assignedMemberName = "John"
            ),
            BillEntity(
                name = "School Tuition & Bus Fee",
                amount = 14500.0,
                dueDateMillis = billDates[3],
                recurrence = "MONTHLY",
                isPaid = false,
                category = "Education",
                assignedMemberName = "John"
            ),
            BillEntity(
                name = "Car Insurance & Maintenance",
                amount = 6500.0,
                dueDateMillis = billDates[4],
                recurrence = "YEARLY",
                isPaid = true,
                category = "Transport",
                assignedMemberName = "Sarah"
            )
        )
        billDao.insertBills(bills)

        // 6. Realistic Transactions
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = timeFormat.format(Date())

        val transactions = listOf(
            // Incomes
            TransactionEntity(
                amount = 85000.0,
                type = "INCOME",
                category = "Salary",
                dateMillis = daysFromNow(-3),
                timeFormatted = "09:30",
                memberId = dadId,
                memberName = "John",
                notes = "Monthly tech salary credited"
            ),
            TransactionEntity(
                amount = 65000.0,
                type = "INCOME",
                category = "Salary",
                dateMillis = daysFromNow(-3),
                timeFormatted = "10:15",
                memberId = momId,
                memberName = "Sarah",
                notes = "Monthly architecture firm compensation"
            ),
            // Expenses
            TransactionEntity(
                amount = 4850.0,
                type = "EXPENSE",
                category = "Groceries",
                dateMillis = daysFromNow(-2),
                timeFormatted = "18:45",
                memberId = dadId,
                memberName = "John",
                notes = "Weekly supermarket fresh produce & dairy"
            ),
            TransactionEntity(
                amount = 2600.0,
                type = "EXPENSE",
                category = "Groceries",
                dateMillis = daysFromNow(-1),
                timeFormatted = "11:20",
                memberId = momId,
                memberName = "Sarah",
                notes = "Artisan bakery and pantry restock"
            ),
            TransactionEntity(
                amount = 3200.0,
                type = "EXPENSE",
                category = "Fuel",
                dateMillis = daysFromNow(-2),
                timeFormatted = "08:15",
                memberId = dadId,
                memberName = "John",
                notes = "SUV full tank gas"
            ),
            TransactionEntity(
                amount = 3750.0,
                type = "EXPENSE",
                category = "Entertainment",
                dateMillis = daysFromNow(-1),
                timeFormatted = "20:30",
                memberId = momId,
                memberName = "Sarah",
                notes = "Family Italian dinner celebration"
            ),
            TransactionEntity(
                amount = 4200.0,
                type = "EXPENSE",
                category = "Shopping",
                dateMillis = daysFromNow(0),
                timeFormatted = "15:10",
                memberId = momId,
                memberName = "Sarah",
                notes = "Kids seasonal apparel & shoes"
            ),
            TransactionEntity(
                amount = 1450.0,
                type = "EXPENSE",
                category = "Education",
                dateMillis = daysFromNow(-1),
                timeFormatted = "16:40",
                memberId = sonId,
                memberName = "Leo",
                notes = "Science & math reference textbooks"
            ),
            TransactionEntity(
                amount = 650.0,
                type = "EXPENSE",
                category = "Entertainment",
                dateMillis = daysFromNow(0),
                timeFormatted = "17:30",
                memberId = sonId,
                memberName = "Leo",
                notes = "Weekend cinema with classmates"
            ),
            TransactionEntity(
                amount = 850.0,
                type = "EXPENSE",
                category = "Education",
                dateMillis = daysFromNow(0),
                timeFormatted = "14:00",
                memberId = daughterId,
                memberName = "Maya",
                notes = "Drawing paper & watercolor sets"
            ),
            TransactionEntity(
                amount = 1299.0,
                type = "EXPENSE",
                category = "Subscriptions",
                dateMillis = daysFromNow(-4),
                timeFormatted = "00:05",
                memberId = dadId,
                memberName = "John",
                notes = "Annual cloud backup & media pass"
            )
        )
        transactionDao.insertTransactions(transactions)
    }

    private fun daysFromNow(days: Int): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, days)
        return cal.timeInMillis
    }
}
