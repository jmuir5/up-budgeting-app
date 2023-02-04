package com.example.upbudgetapp.api

import android.util.Log
import com.example.upbudgetapp.AuthStore
import okhttp3.Interceptor
import okhttp3.Response

object UpAuth : Interceptor {

    private val token: String? get() =
        AuthStore.instance.prefs.getString("apiKey", null)

    override fun intercept(chain: Interceptor.Chain): Response {
        val newRequest = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()
        return chain.proceed(newRequest)
    }

}
