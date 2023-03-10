package com.example.upbudgetapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.upbudgetapp.screens.HomeScreen
import com.example.upbudgetapp.screens.loginCard
import com.example.upbudgetapp.ui.theme.UpBudgetAppTheme
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.seconds

class AuthStore private constructor(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secret_shared_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        lateinit var instance: AuthStore

        fun init(context: Context) {
            if (::instance.isInitialized) return
            val appContext = context.applicationContext
            instance = AuthStore(appContext)
        }

    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AuthStore.init(this)

        //val savedKey = "up:yeah:QFGul2kGiQLYl97cT2LzvncSL5tsCdNvaDOoLmrbW9uWUmxF0uwAYl77atL5CT3cuZed8qcTKIhaI6nTrM1Jax1Vab2U86yzoqJeWwgqOOEhEY5QtHZj8k206TfbvNi3"//sharedPreferences.getString("apikey", "up:yeah:404")
        val savedKey = AuthStore.instance.prefs.getString("apiKey", "up:yeah:404")


        setContent {
            /*val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "buffer") {
                composable("Buffer") { buffer(BufferViewModel(savedKey!!), prefs = sharedPreferences) }
                composable("Login") { loginCard(prefs = sharedPreferences) }
                composable("HomeScreen") { HomeScreen(MainViewModel(savedKey!!))}
                /*...*/
            }*/
            var theme by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                while (true) {
                    delay(3.seconds)
                    theme = true
                }
            }
            UpBudgetAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    NavMain(initKey = savedKey!!, prefs = AuthStore.instance.prefs)
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
    val balance: Long,
    val currency: String
)



data class Transaction(
    val id: String,
    val amount: Long,
    val currency: String,
    val payee: String?=null,
    val payeeLong: String?=null,
    val description: String?=null,
    val accountId: String?=null,
    val categoryId: String?=null,
    val parentCategoryId: String?=null,
    val tag: List<String>?=null
)

data class AppCategory(
    val name: String?=null,
    val total: Long?=null,
    val parentId: String?=null,
    val isParent: Boolean?=null,
    val transactions: List<String>?=null
)

data class Passthrough(
    val accounts: List<Account>,
    val transactions: List<Transaction>,
    val categories: List<AppCategory>,
    var currentAccount: String
)

@Composable
fun Buffer(
    viewModel: BufferViewModel = viewModel(),
    prefs: SharedPreferences,
    navController: NavHostController
) {
    Log.e("started buffer", "HERE")
    while (viewModel.getStatus() == 0) Thread.sleep(10)
    if (viewModel.getStatus() == 200) {
        navController.navigate(Paths.Home.Path + "/${viewModel.getKey()}")
        //HomeScreen(MainViewModel(viewModel.getKey()))
    } else navController.navigate(Paths.Login.Path) {
        navOptions {
            popUpTo(Paths.Buffer.Path) {
                inclusive = true
            }
        }
    }
}

@Composable
fun NavMain(initKey: String, prefs: SharedPreferences) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Paths.Buffer.Path) {
        composable(Paths.Buffer.Path) {
            val viewModel: BufferViewModel = viewModel(
                it, // Make sure ViewModel is dispose when UI is removed.
                factory = BufferViewModel,
                extras = BufferViewModel.creationExtras(apiKey = initKey)
            )
            Buffer(
                viewModel,
                prefs = prefs,
                navController = navController
            )
        }
        composable(Paths.Login.Path) { loginCard(prefs = prefs, navController = navController) }
        composable(Paths.Home.Path + "/{id}") {
            val viewModel: MainViewModel = viewModel(
                it, // Make sure ViewModel is dispose when UI is removed.
                factory = MainViewModel,
                extras = MainViewModel.creationExtras(apiKey = it.arguments?.getString("id")!!)
            )
            val passedKey = it.arguments?.getString("id")
            HomeScreen(viewModel, prefs, navController)
        }
        /*...*/
    }
}






