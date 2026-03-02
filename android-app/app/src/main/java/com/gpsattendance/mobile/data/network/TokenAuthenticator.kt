package com.gpsattendance.mobile.data.network

import com.gpsattendance.mobile.data.local.TokenStore
import com.gpsattendance.mobile.data.model.RefreshTokenRequest
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val tokenStore: TokenStore,
    private val refreshApi: RefreshApi
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null

        val path = response.request.url.encodedPath
        if (path.contains("/api/v1/users/login") || path.contains("/api/v1/users/refresh")) return null

        val refreshToken = runBlocking { tokenStore.getRefreshToken() } ?: return null
        val refreshResponse = refreshApi.refreshBlocking(RefreshTokenRequest(refreshToken)).execute()
        if (!refreshResponse.isSuccessful) {
            runBlocking { tokenStore.clear() }
            return null
        }

        val body = refreshResponse.body() ?: return null
        runBlocking { tokenStore.saveTokens(body.accessToken, body.refreshToken, body.user.name) }

        return response.request.newBuilder()
            .header("Authorization", "Bearer ${body.accessToken}")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}