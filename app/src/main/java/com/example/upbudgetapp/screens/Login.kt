package com.example.upbudgetapp.screens

import android.content.SharedPreferences
import android.text.Layout
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.upbudgetapp.LoginViewModel
import com.example.upbudgetapp.MainViewModel
import com.example.upbudgetapp.Paths
import com.example.upbudgetapp.R
import kotlinx.coroutines.launch


@Composable
fun loginCard(viewModel: LoginViewModel = viewModel(), prefs: SharedPreferences, navController:NavHostController) {
    val coroutineScope = rememberCoroutineScope()
    var text by remember { mutableStateOf("") }
    var response by remember { mutableStateOf(0) }
    var clicked by remember { mutableStateOf(false) }
    Column(
        modifier=Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = CenterHorizontally){

        Image(
            painter = painterResource(R.drawable.shit_up_logo),
            contentDescription = "Shit up logo",
            modifier = Modifier
                // Set image size to 40 dp
                .size(128.dp)
                // Clip image to be shaped as a circle
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colors.secondary, CircleShape)
        )
        Text("Enter your api key")
        TextField(
            value = text,
            onValueChange = {text=it },
            label={ Text("Paste your api key here") })

        //Text("The textfield has this text: "+state)
        Button(
            onClick = {
                response=0
                viewModel.setStatus(0)
                clicked=false
                coroutineScope.launch {viewModel.attemptLogin(text.toString())}

                clicked=true

            }){//LaunchedEffect(true){viewModel.attemptLogin(state.toString())}){
            Text("submit")

        }
        //
        if(clicked==true){
            while(viewModel.getStatus()==0)Thread.sleep(10)
            response=viewModel.getStatus()
            if(response==200){
                prefs.edit().putString("apiKey", text.toString()).apply()
                navController.navigate(Paths.Home.Path+"/${text}"){
                    popUpTo(Paths.Login.Path){
                        inclusive=true
                    }
                    //HomeScreen(MainViewModel(text.toString()))
                }
                clicked=false


            }else if(response==401){
                Text("Your Api Key failed to authenticate")
            }

        }
    }
}

