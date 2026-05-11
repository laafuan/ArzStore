package com.arz.store.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arz.store.model.BannerItem
import com.arz.store.model.CategoryItem
import com.arz.store.model.GameProduct
import com.arz.store.model.Transaction
import com.arz.store.model.TopUpPackage
import com.arz.store.model.UserProfile
import com.arz.store.network.LoginRequest
import com.arz.store.network.RegisterRequest
import com.arz.store.repository.ArzRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class MainViewModel : ViewModel() {
    private var transactionPollingJob: Job? = null
    
    private val _isLoggedIn = MutableStateFlow(ArzRepository.hasToken())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _banners = MutableStateFlow<List<BannerItem>>(emptyList())
    val banners: StateFlow<List<BannerItem>> = _banners.asStateFlow()

    private val _categories = MutableStateFlow<List<CategoryItem>>(emptyList())
    val categories: StateFlow<List<CategoryItem>> = _categories.asStateFlow()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _games = MutableStateFlow<List<GameProduct>>(emptyList())
    val games: StateFlow<List<GameProduct>> = _games.asStateFlow()

    private val _packages = MutableStateFlow<List<TopUpPackage>>(emptyList())
    val packages: StateFlow<List<TopUpPackage>> = _packages.asStateFlow()
    
    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        if (_isLoggedIn.value) {
            loadHomeData()
        }
    }

    fun login(request: LoginRequest, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = ArzRepository.login(request)
            _isLoading.value = false
            if (result.isSuccess) {
                _isLoggedIn.value = true
                loadHomeData()
                onResult(true, null)
            } else {
                onResult(false, result.exceptionOrNull()?.message)
            }
        }
    }

    fun register(request: RegisterRequest, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = ArzRepository.register(request)
            _isLoading.value = false
            if (result.isSuccess) {
                _isLoggedIn.value = true
                loadHomeData()
                onResult(true, null)
            } else {
                onResult(false, result.exceptionOrNull()?.message)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            ArzRepository.logout()
            _isLoggedIn.value = false
            _userProfile.value = null
            _transactions.value = emptyList()
        }
    }

    fun loadHomeData() {
        viewModelScope.launch {
            _isLoading.value = true
            
            _banners.value = ArzRepository.getBanners()
            _categories.value = ArzRepository.getCategories()
            _userProfile.value = ArzRepository.getUserProfile()
            _games.value = ArzRepository.getGames()
            
            _isLoading.value = false
        }
    }

    fun loadPackages(gameId: Int) {
        viewModelScope.launch {
            _packages.value = emptyList() // clear previous
            _packages.value = ArzRepository.getPackagesForGame(gameId)
        }
    }
    
    fun startPollingTransactions() {
        transactionPollingJob?.cancel()
        transactionPollingJob = viewModelScope.launch {
            while (true) {
                try {
                    _transactions.value = ArzRepository.getTransactions()
                } catch (e: Exception) {
                    // Silent fail
                }
                delay(5000)
            }
        }
    }

    fun stopPollingTransactions() {
        transactionPollingJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        stopPollingTransactions()
    }

    fun loadTransactions() {
        viewModelScope.launch {
            _transactions.value = ArzRepository.getTransactions()
        }
    }

    fun loadGames() {
        viewModelScope.launch {
            _games.value = ArzRepository.getGames()
        }
    }

    fun fetchGames() = loadGames()

    fun createTransaction(
        gameId: Int,
        packageId: Int,
        gameUserId: String,
        gameZoneId: String?,
        paymentMethod: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = ArzRepository.createTransaction(
                gameId, packageId, gameUserId, gameZoneId, paymentMethod
            )
            if (result.isSuccess) {
                onResult(true, null)
            } else {
                onResult(false, result.exceptionOrNull()?.message)
            }
        }
    }

    fun updateProfile(name: String, phone: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = ArzRepository.updateProfile(name, phone)
            _isLoading.value = false
            if (result.isSuccess) {
                _userProfile.value = result.getOrNull()
                onResult(true, null)
            } else {
                onResult(false, result.exceptionOrNull()?.message)
            }
        }
    }

    fun updateAvatar(file: java.io.File, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = ArzRepository.updateAvatar(file)
            _isLoading.value = false
            if (result.isSuccess) {
                _userProfile.value = result.getOrNull()
                onResult(true, null)
            } else {
                onResult(false, result.exceptionOrNull()?.message)
            }
        }
    }

    fun changePassword(current: String, new: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = ArzRepository.changePassword(current, new)
            _isLoading.value = false
            if (result.isSuccess) {
                onResult(true, null)
            } else {
                onResult(false, result.exceptionOrNull()?.message)
            }
        }
    }
}
