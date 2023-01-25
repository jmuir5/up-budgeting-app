package com.example.upbudgetapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.upbudgetapp.api.RetrofitHelper
import com.example.upbudgetapp.api.UpApi
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL

class LoginViewModel(
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {

    private var savedKey = ""
    private var responseCode=0
    private val upApi: UpApi

    init {
        viewModelScope.launch {
        }
        this.upApi = RetrofitHelper.instance
    }

    private suspend fun initialise(passedKey:String) = withContext(Dispatchers.Default) {
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

    suspend fun attemptLogin(apiKey:String) = withContext(Dispatchers.IO){
        /*Log.e("head", "Authorisation: Bearer $apiKey")
        val map = HashMap<String, String>()
        map["Authorization"] = "Bearer "+apiKey
        Log.e("map",  map.toString())


        val pingtest = async{
            Log.e("request", upApi.ping(map/*"Authorisation: Bearer $apiKey"*/).headers().toString())
            responseCode=upApi.ping(map/*"Bearer $apiKey"*/).code() }*/
        val pingtest = async{
            val getAccount = URL("https://api.up.com.au/api/v1/util/ping")
            val http: HttpURLConnection = getAccount.openConnection() as HttpURLConnection
            http.setRequestProperty(
                "Authorization",
                "Bearer "+apiKey
            )
            try {
                responseCode = http.responseCode.toInt()
            } finally {
                http.disconnect()
            }
        }
        pingtest.await()
    }


    fun getStatus():Int{
        return responseCode
    }
    fun setStatus(status:Int){
        responseCode=status
    }
    fun getKey():String{
        return savedKey
    }
}
