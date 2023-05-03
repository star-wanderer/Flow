package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.model.*
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl

class UserAuthModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository =
        PostRepositoryImpl(AppDb.getInstance(context = application).postDao())

    private var creds: CredsModel = CredsModel()

    private val _authDataState = MutableLiveData<AuthModelState>()
    val authDataState: LiveData<AuthModelState>
        get() = _authDataState

    val authData: LiveData<AuthModel> = repository.authData

    fun authenticate() {
        viewModelScope.launch {
            try {
                _authDataState.value = AuthModelState(authenticating = true)
                repository.authenticate(creds.login, creds.password)
                _authDataState.value = AuthModelState()
            } catch (e: Exception) {
                _authDataState.value = AuthModelState(error = true)
            }
        }
    }

    fun saveCreds(userCreds: CredsModel){
        creds = userCreds
    }
}
