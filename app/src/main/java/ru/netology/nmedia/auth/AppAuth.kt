package ru.netology.nmedia.auth

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.netology.nmedia.api.Api
import ru.netology.nmedia.model.AuthModel
import ru.netology.nmedia.model.PushToken

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
        sendPushToken()
    }

    val authStateFlow = _authStateFlow.asStateFlow()   // or val authStateFlow : StateFlow<AuthModel> = _authStateFlow

    fun sendPushToken(token: String? = null){
        CoroutineScope(Dispatchers.Default).launch {
            try{
                val pushToken = PushToken (token?: Firebase.messaging.token.await())
                Api.service.save(PushToken(pushToken.token))
            } catch (e: Exception){
                e.printStackTrace()
            }
        }
    }

    fun setUser(user: AuthModel){
        _authStateFlow.value = user
        prefs.edit{
            putLong(ID_KEY, user.id)
            putString(TOKEN_KEY, user.token)
        }
        sendPushToken()
    }

    fun removeUser(){
        _authStateFlow.value = AuthModel()
        prefs.edit{clear()}
        sendPushToken()
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