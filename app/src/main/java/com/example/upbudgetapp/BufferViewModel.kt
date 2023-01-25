package com.example.upbudgetapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import com.example.upbudgetapp.api.RetrofitHelper
import com.example.upbudgetapp.api.UpApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class BufferViewModel(passedKey: String) : ViewModel() {

    private var savedKey = ""
    private var responseCode = 0
    private val upApi: UpApi

    init {
        viewModelScope.launch {
            savedKey = passedKey
            initialise(passedKey)



            Log.e("response code", responseCode.toString())

        }
        this.upApi = RetrofitHelper.instance
    }

    private suspend fun initialise(passedKey: String) = withContext(Dispatchers.Default) {
        // Heavy work]
        val getAccount = URL("https://api.up.com.au/api/v1/util/ping")
        val http: HttpURLConnection = getAccount.openConnection() as HttpURLConnection
        http.setRequestProperty(
            "Authorization",

            "Bearer "+passedKey
        )
        try {
            responseCode = http.responseCode.toInt()
        } finally {
            http.disconnect()
        }
    }

    suspend fun attemptLogin(apiKey: String): Int = withContext(Dispatchers.IO) {
        Log.e("concurrent responseCode before", responseCode.toString())
        Log.e("head", "Authorisation: Bearer $apiKey")
        val map = HashMap<String, String>()

        map["Authorization"] = "Bearer $apiKey"
        Log.e("map",  map.toString())

        val pingtest = async {
            Log.e("request", upApi.ping().headers().toString())
            responseCode = upApi.ping().code()
        }
        pingtest.await()
        Log.e("concurrent responseCode after", responseCode.toString())

        responseCode
    }

    fun getStatus(): Int {
        return responseCode
    }

    fun getKey(): String {
        return savedKey
    }

    companion object : ViewModelProvider.Factory {
        private val API_INIT_KEY = object : CreationExtras.Key<String> {}

        fun creationExtras(apiKey: String): CreationExtras = MutableCreationExtras().apply {
            set(API_INIT_KEY, apiKey)
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            val initKey = requireNotNull(extras[API_INIT_KEY]) {
                "No API key was passed to ViewModel Factory"
            }
            return BufferViewModel(initKey) as T
        }
    }
}
