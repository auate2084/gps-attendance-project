package com.gpsattendance.mobile.data.repository

import android.util.Log
import com.gpsattendance.mobile.data.local.TokenStore
import com.gpsattendance.mobile.data.model.LoginRequest
import com.gpsattendance.mobile.data.model.RegisterRequest
import com.gpsattendance.mobile.data.network.AuthApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenStore: TokenStore
) {
    private companion object {
        const val TAG = "AuthRepository"
    }

    suspend fun register(
        loginId: String,
        email: String,
        password: String,
        name: String,
        teamId: Long
    ): Result<Unit> = runCatching {
        val trimmedLoginId = loginId.trim()
        val trimmedEmail = email.trim()
        val trimmedName = name.trim()
        Log.d(
            TAG,
            "register request: loginId='$trimmedLoginId', email='$trimmedEmail', name='$trimmedName', teamId=$teamId, password=${maskPassword(password)}"
        )

        val response = authApi.register(
            RegisterRequest(trimmedLoginId, trimmedEmail, password, trimmedName, teamId)
        )
        if (!response.isSuccessful) {
            val body = response.errorBody()?.string().orEmpty()
            Log.e(TAG, "register failed: code=${response.code()}, body=$body")
            throw IllegalStateException("Register failed (${response.code()}): $body")
        }

        Log.d(TAG, "register success: code=${response.code()}, loginId='$trimmedLoginId'")
    }

    suspend fun login(loginId: String, password: String): Result<Unit> = runCatching {
        val trimmedLoginId = loginId.trim()
        Log.d(TAG, "login request: loginId='$trimmedLoginId', password=${maskPassword(password)}")

        val response = authApi.login(LoginRequest(trimmedLoginId, password))
        if (!response.isSuccessful) {
            val body = response.errorBody()?.string().orEmpty()
            Log.e(TAG, "login failed: code=${response.code()}, body=$body")
            throw IllegalStateException("Login failed (${response.code()}): $body")
        }
        val body = response.body() ?: throw IllegalStateException("Login response body is empty")
        tokenStore.saveTokens(body.accessToken, body.refreshToken, body.user.name)
        Log.d(TAG, "login success: loginId='$trimmedLoginId', user='${body.user.name}'")
    }

    suspend fun isLoggedIn(): Boolean = !tokenStore.getAccessToken().isNullOrBlank()

    suspend fun currentUserName(): String? = tokenStore.getUserName()

    suspend fun logout() {
        tokenStore.clear()
    }

    private fun maskPassword(password: String): String {
        return "len=${password.length}"
    }
}
