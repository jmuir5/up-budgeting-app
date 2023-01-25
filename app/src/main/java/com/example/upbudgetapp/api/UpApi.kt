package com.example.upbudgetapp.api

import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.upbudgetapp.Transaction
import com.example.upbudgetapp.screens.HomeScreen
import com.example.upbudgetapp.screens.loginCard
import com.example.upbudgetapp.ui.theme.UpBudgetAppTheme
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Headers
import java.io.BufferedReader
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

interface UpApi {
    @GET("/api/v1/accounts")
    suspend fun getAccounts(): Response<Any>

    @GET("/api/v1/transactions")
    suspend fun getTransactions(): Response<ApiTransactionResponse>

    @GET("/api/v1/util/ping")
    suspend fun ping(): Response<Any>

}

//fun ApiTransaction.toTransaction() =
//    Transaction(
//
//    )

object RetrofitHelper {

    const val baseUrl = "https://api.up.com.au/api/v1/"

    private val contentType = "application/json".toMediaType()
    val instance: UpApi by lazy {
        Retrofit.Builder().baseUrl(baseUrl)
            .addConverterFactory(Json.asConverterFactory(contentType))
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(UpAuth)
                    .addInterceptor(HttpLoggingInterceptor())
                    .build()
            )
            // we need to add converter factory to
            // convert JSON object to Java object
            .build()
            .create()
    }
}

@Serializable
data class ApiTransactionResponse(
    val data: List<ApiTransaction>,
)

@Serializable
data class ApiAmount(
    val currencyCode: String,
    val value: String,
    val valueInBaseUnits: Long,
)

@Serializable
data class ApiTransaction(
    val attributes: ApiTransactionAttributes,
)

@Serializable
data class ApiTransactionAttributes(
    val amount: ApiAmount
)
