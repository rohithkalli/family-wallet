package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.FamilyWalletRepository

class WalletViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WalletViewModel::class.java)) {
            val db = AppDatabase.getInstance(context.applicationContext)
            val repository = FamilyWalletRepository(db)
            return WalletViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
