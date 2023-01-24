package com.example.upbudgetapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class BufferViewModel(passedKey: String) : ViewModel() {

    private var savedKey = ""
    private var responseCode=0
    private val upApi: UpApi
    init {
        viewModelScope.launch {
            savedKey=passedKey
            initialise(passedKey)



            Log.e("response code", responseCode.toString())

        }
        this.upApi = RetrofitHelper.instance
    }

    private suspend fun initialise(passedKey:String) = withContext(Dispatchers.Default) {
        // Heavy work]
        val getAccount = URL("https://api.up.com.au/api/v1/util/ping")
        val http: HttpURLConnection = getAccount.openConnection() as HttpURLConnection
        http.setRequestProperty(
            "Authorization",
            "Bearer "+passedKey//up:yeah:GFGul2kGiQLYl97cT2LzvncSL5tsCdNvaDOoLmrbW9uWUmxF0uwAYl77atL5CT3cuZed8qcTKIhaI6nTrM1Jax1Vab2U86yzoqJeWwgqOOEhEY5QtHZj8k206TfbvNi3"//up:yeah:404"
        )
        try {
            responseCode = http.responseCode.toInt()
        } finally {
            http.disconnect()
        }
    }

    suspend fun attemptLogin(apiKey:String):Int = withContext(Dispatchers.IO){
        Log.e("concurrent responseCode before", responseCode.toString())
        Log.e("head", "Authorisation: Bearer $apiKey")
        val map = HashMap<String, String>()
        map["Authorization"] = "Bearer up:yeah:QFGul2kGiQLYl97cT2LzvncSL5tsCdNvaDOoLmrbW9uWUmxF0uwAYl77atL5CT3cuZed8qcTKIhaI6nTrM1Jax1Vab2U86yzoqJeWwgqOOEhEY5QtHZj8k206TfbvNi3"
        Log.e("map",  map.toString())

        val pingtest = async{
            Log.e("request", upApi.ping(map/*"Authorisation: Bearer $apiKey"*/).headers().toString())
            responseCode=upApi.ping(map/*"Bearer $apiKey"*/).code() }
        pingtest.await()
        Log.e("concurrent responseCode after", responseCode.toString())

        responseCode


    }
    fun getStatus():Int{
        return responseCode
    }
    fun getKey():String{
        return savedKey
    }
}
