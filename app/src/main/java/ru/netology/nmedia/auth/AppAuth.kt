package ru.netology.nmedia.auth

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.netology.nmedia.model.AuthModel

class AppAuth private constructor(context: Context){
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private var _authStateFlow: MutableStateFlow<AuthModel>

    init {
        val id = prefs.getLong(ID_KEY, 0)
        val token = prefs.getString(TOKEN_KEY, null)

        if (id == 0L || token == null){
            prefs.edit{ clear() }
            _authStateFlow = MutableStateFlow(AuthModel())
        } else {
            _authStateFlow = MutableStateFlow(AuthModel(id, token))
        }
    }

    val authStateFlow = _authStateFlow.asStateFlow()   // or val authStateFlow : StateFlow<AuthModel> = _authStateFlow

    fun setUser(user: AuthModel){
        _authStateFlow.value = user
        prefs.edit{
            putLong(ID_KEY, user.id)
            putString(TOKEN_KEY, user.token)
        }
    }

    fun removeUser(){
        _authStateFlow.value = AuthModel()
        prefs.edit{clear()}
    }

    companion object{
        private const val ID_KEY = "ID_KEY"
        private const val TOKEN_KEY = "TOKEN_KEY"

        @Volatile
        private var instance: AppAuth? = null

        @Synchronized
        fun initAppAuth(context: Context): AppAuth {
            return instance?: AppAuth(context).apply { instance = this }
        }

        fun getInstance(): AppAuth = requireNotNull(instance){"init AppAuth was not invoked"}
    }
}