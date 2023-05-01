package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.Dispatchers
import ru.netology.nmedia.auth.AppAuth

class AppAuthModel: ViewModel(){
    val authLiveData = AppAuth.getInstance().authStateFlow.asLiveData(Dispatchers.Main)
    val isAuthorized: Boolean
        get() = AppAuth.getInstance().authStateFlow.value.token != null
}