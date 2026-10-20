package com.nandaadisaputra.onlineshop.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.nandaadisaputra.onlineshop.data.session.UserPreference
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class SplashViewModel(application: Application) : AndroidViewModel(application) {

    private val userPreference = UserPreference(application)

    // LiveData untuk menentukan status navigasi tujuan setelah splash selesai
    private val _navigateTo = MutableLiveData<SplashDestination>()
    val navigateTo: LiveData<SplashDestination> get() = _navigateTo

    init {
        checkSessionAndDelay()
    }

    private fun checkSessionAndDelay() {
        viewModelScope.launch {
            // Berikan jeda splash screen (misal 1.5 detik agar logo terlihat)
            delay(1500.milliseconds)

            // Cek apakah user sudah login
            if (userPreference.isLoggedIn()) {
                val role = userPreference.getRole() ?: "user"
                _navigateTo.value = SplashDestination.Dashboard(role)
            } else {
                _navigateTo.value = SplashDestination.Login
            }
        }
    }
}

// Sealed class untuk merepresentasikan tujuan navigasi secara aman (Type-safe)
sealed class SplashDestination {
    data class Dashboard(val role: String) : SplashDestination()
    object Login : SplashDestination()
}