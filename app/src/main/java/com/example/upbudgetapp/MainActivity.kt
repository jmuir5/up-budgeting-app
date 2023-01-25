package com.example.upbudgetapp

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
import com.example.upbudgetapp.screens.HomeScreen
import com.example.upbudgetapp.screens.loginCard
import com.example.upbudgetapp.ui.theme.UpBudgetAppTheme
import kotlinx.coroutines.*
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
    suspend fun getAccounts(@HeaderMap headers:Map<String, String>) : Response<Any>

    @GET("/api/v1/transactions")
    suspend fun getTransactions(@HeaderMap headers:Map<String, String>) : Response<Any>

    @GET("/api/v1/util/ping")
    suspend fun ping(@HeaderMap headers:Map<String, String>) : Response<Any>

}

object RetrofitHelper {

    const val baseUrl = "https://api.up.com.au/api/v1/"

    val instance: UpApi by lazy {
        Retrofit.Builder().baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val masterKey = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(this,
            "secret_shared_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        val savedKey = sharedPreferences.getString("apiKey", "up:yeah:404")

        setContent {
            /*val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "buffer") {
                composable("Buffer") { buffer(BufferViewModel(savedKey!!), prefs = sharedPreferences) }
                composable("Login") { loginCard(prefs = sharedPreferences) }
                composable("HomeScreen") { HomeScreen(MainViewModel(savedKey!!))}
                /*...*/
            }*/
            UpBudgetAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    navMain(initKey = savedKey!!, prefs = sharedPreferences)
                    //navController.navigate("buffer")
                    //Buffer(BufferViewModel(savedKey!!), sharedPreferences)
                    //HomeScreen()

                }
            }
        }
    }
}

data class Message(val author: String, val body: String)
data class Account(
    val id: String,
    val name: String,
    val balance: Float,
    val currency: String
)
data class Transaction(
    val id: String,
    val amount: Float,
    val currency: String,
    val payee: String,
    val payeeLong:String,
    val description:String,
    val accountId: String,
    val categoryId: String,
    val parentCategoryId: String,
    val tag:List<String>
)
data class Category(
    val name:String,
    val total:Float,
    val parentId:String,
    val isParent:Boolean,
    val transactions:List<String>
)
data class Passthrough(
    val accounts:List<Account>,
    val transactions:List<Transaction>,
    val categories:List<Category>,
    var currentAccount:String
)

@Composable
fun Buffer(viewModel: BufferViewModel = viewModel(), prefs: SharedPreferences, navController: NavHostController){
    Log.e("started buffer", "HERE")
    while(viewModel.getStatus()==0)Thread.sleep(10)
    if(viewModel.getStatus()==200){
        navController.navigate(Paths.Home.Path+"/${viewModel.getKey()}")
        //HomeScreen(MainViewModel(viewModel.getKey()))
    }
    else navController.navigate(Paths.Login.Path)//loginCard(prefs=prefs)
}

@Composable
fun navMain(initKey: String, prefs: SharedPreferences){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Paths.Buffer.Path) {
        composable(Paths.Buffer.Path) { Buffer(BufferViewModel(initKey!!), prefs = prefs, navController = navController) }
        composable(Paths.Login.Path) { loginCard(prefs = prefs, navController = navController) }
        composable(Paths.Home.Path+"/{id}") { navBackStack ->
            val passedKey = navBackStack.arguments?.getString("id")
            HomeScreen(MainViewModel(passedKey!!), prefs, navController) }
        /*...*/
    }
}






