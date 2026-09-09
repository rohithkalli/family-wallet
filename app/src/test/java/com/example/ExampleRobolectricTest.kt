package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.FamilyMemberEntity
import com.example.domain.model.BudgetProgress
import com.example.domain.model.FamilyFinancialSummary
import com.example.domain.model.MemberFinancialSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Family Wallet", appName)
    }

    @Test
    fun `test family financial balance equation`() {
        val income = 120000.0
        val expenses = 45000.0
        val savings = 35000.0
        val summary = FamilyFinancialSummary(
            totalIncome = income,
            totalExpense = expenses,
            totalBalance = income - expenses,
            totalSavings = savings,
            upcomingBillsCount = 2
        )

        assertEquals(75000.0, summary.totalBalance, 0.01)
        assertEquals(120000.0, summary.totalIncome, 0.01)
        assertEquals(45000.0, summary.totalExpense, 0.01)
        assertEquals(35000.0, summary.totalSavings, 0.01)
    }

    @Test
    fun `test budget warning threshold and over budget`() {
        val budget = BudgetEntity(id = 1, category = "Groceries", monthlyLimit = 15000.0)

        // Under 80%
        val progressNormal = BudgetProgress(
            budget = budget,
            spent = 10000.0,
            remaining = 5000.0,
            percentage = (10000.0 / 15000.0).toFloat(),
            isCloseToBudget = false,
            isOverBudget = false
        )
        assertFalse(progressNormal.isCloseToBudget)
        assertFalse(progressNormal.isOverBudget)

        // Near 80%
        val progressNear = BudgetProgress(
            budget = budget,
            spent = 12500.0,
            remaining = 2500.0,
            percentage = (12500.0 / 15000.0).toFloat(),
            isCloseToBudget = true,
            isOverBudget = false
        )
        assertTrue(progressNear.isCloseToBudget)
        assertFalse(progressNear.isOverBudget)

        // Over limit
        val progressOver = BudgetProgress(
            budget = budget,
            spent = 16000.0,
            remaining = 0.0,
            percentage = (16000.0 / 15000.0).toFloat(),
            isCloseToBudget = false,
            isOverBudget = true
        )
        assertTrue(progressOver.isOverBudget)
    }

    @Test
    fun `test member allowance remaining and net contribution`() {
        val member = FamilyMemberEntity(
            id = 1,
            name = "John",
            role = "Father",
            monthlyIncome = 85000.0,
            monthlyAllowance = 15000.0,
            colorHex = 0xFF1E88E5
        )

        val memberSummary = MemberFinancialSummary(
            member = member,
            totalIncome = 85000.0,
            totalExpense = 8200.0,
            allowanceSpent = 8200.0,
            allowanceRemaining = 15000.0 - 8200.0,
            netContribution = 85000.0 - 8200.0,
            contributionPercent = 70.8f
        )

        assertEquals(6800.0, memberSummary.allowanceRemaining, 0.01)
        assertEquals(76800.0, memberSummary.netContribution, 0.01)
    }
}
