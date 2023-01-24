package com.example.upbudgetapp.screens

import android.content.SharedPreferences
import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.R
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.upbudgetapp.*
import com.example.upbudgetapp.ui.theme.UpBudgetAppTheme
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(viewModel: MainViewModel = viewModel(), prefs:SharedPreferences, navController:NavHostController) {
    while(viewModel.getCategories().size==0)Thread.sleep(10)
    val passthrough = Passthrough(viewModel.getAccounts().toList(), viewModel.getTransactions().toList(), viewModel.getCategories().toList(), "")
    Column() {
        Button(
            onClick = {
                prefs.edit().remove("apiKey").apply()
                navController.navigate(Paths.Login.Path){
                    popUpTo(Paths.Home.Path){
                        inclusive=true
                    }
                }
            }
        ) {//LaunchedEffect(true){viewModel.attemptLogin(state.toString())}){
            Text("logout")

        }
        LazyColumn() {
            items(viewModel.getAccounts()) { account ->
                HeaderCard(account, passthrough)
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Composable
fun HeaderCard(info: Account, passthrough:Passthrough){
    val passthrough2 = Passthrough(passthrough.accounts, passthrough.transactions, passthrough.categories, info.id)
    Column(){
        var isExpanded by remember { mutableStateOf(false) }
        Row(modifier = Modifier
            .padding(all = 8.dp)
            .clickable { isExpanded = !isExpanded }) {
            Image(
                painter = painterResource(com.example.upbudgetapp.R.drawable.profile_picture),
                contentDescription = "Contact profile picture",
                modifier = Modifier
                    // Set image size to 40 dp
                    .size(80.dp)
                    // Clip image to be shaped as a circle
                    .clip(CircleShape)
                    .border(1.5.dp, MaterialTheme.colors.secondaryVariant, CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column() {
                Text(
                    text = "Account: ${info.name}",
                    color = MaterialTheme.colors.secondaryVariant,
                    style = MaterialTheme.typography.h5
                )
                Text(
                    text = "Balance: \$${(info.balance / 100)} ${info.currency}",
                    color = MaterialTheme.colors.secondaryVariant,
                    style = MaterialTheme.typography.h5
                )
            }
            Spacer(Modifier.weight(1f))
        }
        if (isExpanded){
            Log.e("category", passthrough.categories.indices.toString())
            Log.e("category", passthrough.categories.toString())
            for(i in passthrough.categories){
                if(i.isParent) {
                    var flag=0
                    for (j in i.transactions){
                        for(k in passthrough2.transactions){
                            if(j==k.id&&k.accountId==info.id) flag=1
                        }
                    }
                    if(flag==1)CategoryCard(i, passthrough2, 0)
                }
            }
        }
    }
}

@Composable
fun CategoryCard(info: Category, passthrough:Passthrough, level:Int){
    val pad = 16+(level*8)

    Column() {
        var isExpanded by remember { mutableStateOf(false) }
        Row(modifier = Modifier
            .padding(all = 8.dp)
            .padding(start = pad.dp)
            .clickable { isExpanded = !isExpanded }) {
            Image(
                painter = painterResource(com.example.upbudgetapp.R.drawable.profile_picture),
                contentDescription = "Contact profile picture",
                modifier = Modifier
                    // Set image size to 40 dp
                    .size(60.dp)
                    // Clip image to be shaped as a circle
                    .clip(CircleShape)
                    .border(1.5.dp, MaterialTheme.colors.secondary, CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            var catName = info.name
            if(info.name == "null")catName = "Uncategorised"
            Column() {
                Text(
                    text = catName,
                    color = MaterialTheme.colors.secondaryVariant,
                    style = MaterialTheme.typography.h6
                )
                Text(
                    text = "\$${(info.total / 100)} AUD",
                    color = MaterialTheme.colors.secondaryVariant,
                    style = MaterialTheme.typography.h6
                )
            }
            Spacer(Modifier.weight(1f))
        }
        if (isExpanded) {
            if (info.isParent && info.name != "null") {
                for (i in passthrough.categories) {
                    if (!i.isParent) {
                        var flag=0
                        for (j in i.transactions){
                            for(k in passthrough.transactions){
                                if(j==k.id&&k.accountId==passthrough.currentAccount) flag=1
                            }
                        }
                        if(flag==1)CategoryCard(i, passthrough, level+1)
                    }
                }
            } else {
                for (i in passthrough.transactions) {
                    if (i.categoryId == info.name&&i.accountId==passthrough.currentAccount) {
                        TransactionCard(i, passthrough, level+1)
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionCard(info: Transaction, passthrough:Passthrough, level:Int){
    val pad = 16+(level*8)
    Column() {
        var isExpanded by remember { mutableStateOf(false) }
        Row(modifier = Modifier
            .padding(all = 8.dp)
            .padding(start = pad.dp)
            .clickable { isExpanded = !isExpanded }) {
            Image(
                painter = painterResource(com.example.upbudgetapp.R.drawable.profile_picture),
                contentDescription = "Contact profile picture",
                modifier = Modifier
                    // Set image size to 40 dp
                    .size(40.dp)
                    // Clip image to be shaped as a circle
                    .clip(CircleShape)
                    .border(1.5.dp, MaterialTheme.colors.secondary, CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Row(){
                Column() {
                    var transacDesc = info.description
                    if(transacDesc== "null") transacDesc = ""
                    Text(
                        text = info.payee,
                        color = MaterialTheme.colors.secondaryVariant,
                        style = MaterialTheme.typography.subtitle1
                    )
                    Text(
                        text = (transacDesc),
                        color = MaterialTheme.colors.secondaryVariant,
                        style = MaterialTheme.typography.subtitle1
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "\$${(info.amount / 100)}",
                    color = MaterialTheme.colors.secondaryVariant,
                    style = MaterialTheme.typography.h4
                )
            }
        }
        /*if (isExpanded) {
            if (info.isParent && info.name != "null") {
                for (i in passthrough.categories) {
                    if (!i.isParent) {
                        CategoryCard(i, passthrough)
                    }
                }
            } else {
                Log.e("category name", info.name)
                for (i in passthrough.transactions) {
                    Log.e("parent category id", i.parentCategoryId)
                    if (i.categoryId == info.name) {
                        //transactionCard(i, passthrough)
                        GreetingCard(Message("system", "successfull transaction"))
                    }
                }
            }
        }*/
    }
}

@Composable
fun GreetingCard(msg: Message) {
    Row(modifier = Modifier.padding(all = 8.dp)) {
        Image(
            painter = painterResource(com.example.upbudgetapp.R.drawable.profile_picture),
            contentDescription = "Contact profile picture",
            modifier = Modifier
                // Set image size to 40 dp
                .size(40.dp)
                // Clip image to be shaped as a circle
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colors.secondary, CircleShape)
        )


        // Add a horizontal space between the image and the column
        Spacer(modifier = Modifier.width(8.dp))
        var isExpanded by remember { mutableStateOf(false) }
        /*val surfaceColor by animateColorAsState(
            if (isExpanded) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
        )*/

        // We toggle the isExpanded variable when we click on this Column
        Column(modifier = Modifier.clickable { isExpanded = !isExpanded }) {
            Text(text = msg.author,
                color = MaterialTheme.colors.secondaryVariant,
                style = MaterialTheme.typography.subtitle2
            )

            // Add a vertical space between the author and message texts
            Spacer(modifier = Modifier.height(4.dp))
            Surface(shape = MaterialTheme.shapes.medium, elevation = 1.dp) {
                Text(
                    text = msg.body,
                    modifier = Modifier.padding(all = 4.dp),
                    // If the message is expanded, we display all its content
                    // otherwise we only display the first line
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    style = MaterialTheme.typography.body2
                )
            }
        }
    }
}

@Composable
fun Conversation(messages: List<Message>) {
    LazyColumn {
        items(messages) { message ->
            GreetingCard(message)
        }
    }
}


@Preview(name="light mode",
    showBackground = true)
@Preview(showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    name = "dark mode")
@Composable
fun DefaultPreview() {
    UpBudgetAppTheme {
        val testMessage = Message("test", "message")
        GreetingCard(testMessage)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewConversation() {
    UpBudgetAppTheme {
        Conversation(SampleData.conversationSample)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    UpBudgetAppTheme {
        //HomeScreen()
    }
}