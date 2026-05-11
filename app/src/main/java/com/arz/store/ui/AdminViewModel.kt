package com.arz.store.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arz.store.model.GameProduct
import com.arz.store.model.Transaction
import com.arz.store.repository.ArzRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class AdminViewModel : ViewModel() {
    private var pollingJob: Job? = null

    private val _allTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val allTransactions: StateFlow<List<Transaction>> = _allTransactions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun startPollingTransactions() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                try {
                    val transactions = ArzRepository.getAllTransactions()
                    _allTransactions.value = transactions
                } catch (e: Exception) {
                    // Silent fail for polling
                }
                delay(5000) // Poll every 5 seconds
            }
        }
    }

    fun stopPollingTransactions() {
        pollingJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        stopPollingTransactions()
    }

    fun loadAllTransactions() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val transactions = ArzRepository.getAllTransactions()
                _allTransactions.value = transactions
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTransactionStatus(id: String, status: String, notes: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = ArzRepository.updateTransactionStatus(id, status, notes)
            if (result.isSuccess) {
                // Refresh transactions after update
                loadAllTransactions()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Gagal mengupdate status transaksi"
            }
            _isLoading.value = false
        }
    }

    fun createGame(
        name: String, slug: String, categoryId: Int,
        description: String?, gradientStart: String?, gradientEnd: String?, accentColor: String?,
        isPopular: Boolean, isNew: Boolean, requiresZoneId: Boolean, sortOrder: Int,
        iconFile: File?, bannerFile: File?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = ArzRepository.createGame(
                name, slug, categoryId, description, gradientStart, gradientEnd, accentColor,
                isPopular, isNew, requiresZoneId, sortOrder, iconFile, bannerFile
            )
            if (result.isSuccess) {
                onSuccess()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Gagal menambahkan game"
            }
            _isLoading.value = false
        }
    }

    fun deleteGame(id: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = ArzRepository.deleteGame(id)
            if (result.isSuccess) {
                onSuccess()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Gagal menghapus game"
            }
            _isLoading.value = false
        }
    }

    fun createPackage(
        gameId: Int, label: String, amount: Int, bonus: Int, price: Long, isPopular: Boolean,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = ArzRepository.createPackage(gameId, label, amount, bonus, price, isPopular)
            if (result.isSuccess) onSuccess()
            else _error.value = result.exceptionOrNull()?.message
            _isLoading.value = false
        }
    }

    fun deletePackage(id: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = ArzRepository.deletePackage(id)
            if (result.isSuccess) onSuccess()
            else _error.value = result.exceptionOrNull()?.message
            _isLoading.value = false
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}
