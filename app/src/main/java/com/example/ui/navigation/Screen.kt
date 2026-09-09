package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val inBottomBar: Boolean = true
) {
    Dashboard("dashboard", "Dashboard", Icons.Default.Dashboard),
    Transactions("transactions", "Transactions", Icons.Default.ReceiptLong),
    Budgets("budgets", "Budgets", Icons.Default.PieChart),
    Bills("bills", "Bills", Icons.Default.CreditCard),
    Family("family", "Family", Icons.Default.FamilyRestroom),
    Analytics("analytics", "Analytics", Icons.Default.Analytics, inBottomBar = false);

    companion object {
        val bottomBarScreens = listOf(Dashboard, Transactions, Budgets, Bills, Family)
    }
}
