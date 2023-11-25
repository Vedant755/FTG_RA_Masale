package com.ftg.famasale.Utils

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class ServerCallInterceptor @Inject constructor() : Interceptor {
    @Inject
    lateinit var tokenManager: SharedPrefManager

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
        val token = tokenManager.getToken()
        request.addHeader("Authorization", "Bearer $token")
        request.addHeader("User-Agent", "Android")
        return chain.proceed(request.build())
    }
}