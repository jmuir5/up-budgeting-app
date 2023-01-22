package com.example.upbudgetapp

import okhttp3.Interceptor
import okhttp3.Response

object UpAuth : Interceptor {

    // total hack for now, should be private later
    var token: String = ""

    override fun intercept(chain: Interceptor.Chain): Response {
        val newRequest = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .build()

        return chain.proceed(newRequest)
    }

}
