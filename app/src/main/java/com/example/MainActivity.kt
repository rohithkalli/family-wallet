package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.navigation.FamilyWalletApp
import com.example.ui.theme.FamilyWalletTheme
import com.example.ui.viewmodel.WalletViewModel
import com.example.ui.viewmodel.WalletViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: WalletViewModel by viewModels {
        WalletViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FamilyWalletTheme {
                FamilyWalletApp(viewModel = viewModel)
            }
        }
    }
}
