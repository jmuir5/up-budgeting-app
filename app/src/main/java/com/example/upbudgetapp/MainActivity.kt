package com.example.upbudgetapp

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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.upbudgetapp.ui.theme.UpBudgetAppTheme
import java.net.HttpURLConnection
import java.net.URL
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStream


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UpBudgetAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {

                    HomeScreen()

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

class MyViewModel: ViewModel() {
    init {
        viewModelScope.launch {
            // Coroutine that will be canceled when the ViewModel is cleared.
            massPopulate()
        }

    }
    private var accounts:MutableList<Account> = mutableListOf()
    private var transactions:MutableList<Transaction> = mutableListOf()
    private var categories:MutableList<Category> = mutableListOf()

    suspend fun massPopulate() = withContext(Dispatchers.Default){
        populateAccounts()
        Log.e("accounts", "finished accounts")
        populateTransactions()
        Log.e("status", "finished transactions")
        populateCategories()
        Log.e("status", "finished categories")
    }
    suspend fun populateAccounts() = withContext(Dispatchers.Default) {
        // Heavy work
        val getAccount = URL("https://api.up.com.au/api/v1/accounts")
        val http: HttpURLConnection = getAccount.openConnection() as HttpURLConnection
        http.setRequestProperty(
            "Authorization",
            "Bearer up:yeah:QFGul2kGiQLYl97cT2LzvncSL5tsCdNvaDOoLmrbW9uWUmxF0uwAYl77atL5CT3cuZed8qcTKIhaI6nTrM1Jax1Vab2U86yzoqJeWwgqOOEhEY5QtHZj8k206TfbvNi3"
        )
        try {
            val result = http.content as InputStream
            val resultReader = BufferedReader(result.reader())
            val content: String
            try {
                content = resultReader.readText()
            } finally {
                resultReader.close()
            }
            val delimiter = "},{\"type\":\"accounts\","
            val delimiter2 = "\"data\":[{\"type\":\"accounts\","
            val delimiter3 = "}}],"
            val parts = content.split(delimiter, delimiter2, delimiter3)
            for(i in parts) {
                if (i.startsWith("\"id\":")) {
                    Log.e("response code", http.getResponseCode().toString())
                    Log.e("response body", content)
                    accounts.add(
                        Account(
                            getId(i),
                            getAccountName(i),
                            getAccountBalance(i),
                            getAccountCurrency(i)
                        )
                    )
                    Log.e("processed accounts", accounts.toString())
                    Log.e("processed accounts", accounts.size.toString())
                }
            }
        }finally {
            http.disconnect()
        }
    }
    suspend fun populateTransactions() = withContext(Dispatchers.Default) {
        // Heavy work
        val getAccount = URL("https://api.up.com.au/api/v1/transactions")
        val http: HttpURLConnection = getAccount.openConnection() as HttpURLConnection
        http.setRequestProperty(
            "Authorization",
            "Bearer up:yeah:QFGul2kGiQLYl97cT2LzvncSL5tsCdNvaDOoLmrbW9uWUmxF0uwAYl77atL5CT3cuZed8qcTKIhaI6nTrM1Jax1Vab2U86yzoqJeWwgqOOEhEY5QtHZj8k206TfbvNi3"
        )
        try {
            val result = http.content as InputStream
            val resultReader = BufferedReader(result.reader())
            val content: String
            try {
                content = resultReader.readText()
            } finally {
                resultReader.close()
            }
            val delimiter = "},{\"type\":\"transactions\","
            val delimiter2 = "\"data\":[{\"type\":\"transactions\","
            val delimiter3 = "}}],"
            val parts = content.split(delimiter, delimiter2, delimiter3)
            Log.e("response code", http.getResponseCode().toString())
            Log.e("response body", content)
            for(i in parts) {
                if(i.startsWith("\"id\":")) {
                    transactions.add(
                        Transaction(
                            getId(i),
                            getTransactionAmount(i),
                            getTransactionCurrency(i),
                            getTransactionPayee(i),
                            getTransactionPayeeL(i),
                            getTransactionDesc(i),
                            getAccountId(i),
                            getTransactionCategory(i),
                            getTransactionParentCategory(i),
                            getTransactionTags(i)
                        )
                    )
                }
            }
            Log.e("processed transactions", transactions.toString())
            Log.e("processed transactions", transactions.size.toString())
        }finally {
            http.disconnect()
        }
    }

    fun populateCategories(){
        val catNames = mutableListOf<String>()
        val catAmounts = mutableListOf<Float>()
        val catId = mutableListOf<MutableList<String>>()
        val catParent = mutableListOf<String>()
        val catIsParent = mutableListOf<Boolean>()
        for(i in transactions)
            if(i.parentCategoryId!="null"){
                if(!catNames.contains(i.categoryId)) {
                    catNames.add(i.parentCategoryId)
                    catAmounts.add(i.amount)
                    catId.add(mutableListOf(i.id))
                    catParent.add(i.parentCategoryId)
                    catIsParent.add(true)
                }
                if(catNames.contains(i.categoryId)){
                    val index = catNames.indexOf(i.categoryId)
                    catAmounts[index]+=i.amount
                    catId[index].add(i.id)
                }
            }
        for(i in transactions){
            if(catNames.contains(i.categoryId)){
                val index = catNames.indexOf(i.categoryId)
                catAmounts[index]+=i.amount
                catId[index].add(i.id)
            }
            if(!catNames.contains(i.categoryId)){
                catNames.add(i.categoryId)
                catAmounts.add(i.amount)
                catId.add(mutableListOf(i.id))
                catParent.add(i.parentCategoryId)
                if(i.parentCategoryId=="null") catIsParent.add(true)
                else catIsParent.add(false)
            }
        }
        for(i in catNames.indices){
            categories.add(Category(catNames[i], catAmounts[i], catParent[i], catIsParent[i], catId[i]))
        }
    }

    /*fun getAccountType(content:String): String {
        var delimiter = "\",\""
        val parts = content.split(delimiter)
        var id = ""
        for (i in parts) {
            Log.e("name parts", i)
            if (i.startsWith("id")) {
                var delimiter = "\":\""
                val delimiter2= "\",\""
                return i.split(delimiter)[1]
            }
        }
        return "no id found"
    }*/
    fun getId(content:String): String {
        var delimiter = "\"id\":\""
        val parts = content.split(delimiter)
        delimiter = "\""
        return parts[1].split(delimiter)[0]
    }
    fun getAccountName(content:String): String {
        var delimiter = "\"displayName\":\""
        val parts = content.split(delimiter)
        delimiter = "\""
        return parts[1].split(delimiter)[0]
    }
    fun getAccountBalance(content:String): Float {
        var delimiter = "\"valueInBaseUnits\":"
        val parts = content.split(delimiter)
        delimiter = "}"
        return parts[1].split(delimiter)[0].toFloat()
    }
    fun getAccountCurrency(content:String): String {
        var delimiter = "\"currencyCode\":\""
        val parts = content.split(delimiter)
        delimiter = "\""
        return parts[1].split(delimiter)[0]
    }
    fun getTransactionAmount(content:String): Float {
        var delimiter = "\"amount\":"
        var parts = content.split(delimiter)
        delimiter = "valueInBaseUnits\":"
        parts = parts[1].split(delimiter)
        delimiter = "}"
        return parts[1].split(delimiter)[0].toFloat()
    }
    fun getTransactionCurrency(content:String): String {
        var delimiter = "\"amount\":"
        var parts = content.split(delimiter)
        delimiter = "currencyCode\":\""
        parts = parts[1].split(delimiter)
        delimiter = "\""
        return parts[1].split(delimiter)[0]
    }
    fun getTransactionPayee(content:String): String {
        var delimiter = "\"attributes\":{"
        var parts = content.split(delimiter)
        delimiter = "description\":"
        parts = parts[1].split(delimiter)
        if(parts[1].startsWith("null"))return "null"
        delimiter = "\""
        return parts[1].split(delimiter)[1]
    }
    fun getTransactionPayeeL(content:String): String {
        var delimiter = "\"attributes\":{"
        var parts = content.split(delimiter)
        delimiter = "rawText\":"
        parts = parts[1].split(delimiter)
        if(parts[1].startsWith("null"))return "null"
        delimiter = "\""
        return parts[1].split(delimiter)[1]
    }
    fun getTransactionDesc(content:String): String {
        var delimiter = "\"attributes\":{"
        var parts = content.split(delimiter)
        //for(i in parts)Log.e("desc", i)
        delimiter = "message\":"
        parts = parts[1].split(delimiter)
        //for(i in parts)Log.e("transaction", i)
        if(parts[1].startsWith("null"))return "null"
        delimiter = "\""
        return parts[1].split(delimiter)[1]
    }
    fun getAccountId(content:String): String {
        var delimiter = "\"account\":"
        var parts = content.split(delimiter)
        delimiter = "id\":\""
        parts = parts[1].split(delimiter)
        delimiter = "\""
        return parts[1].split(delimiter)[0]
    }
    fun getTransactionCategory(content:String): String {
        var delimiter = "\"category\":{\"data\":"
        var parts = content.split(delimiter)
        if(parts[1].startsWith("null"))return "null"
        delimiter = "id\":\""
        parts = parts[1].split(delimiter)
        delimiter = "\""
        return parts[1].split(delimiter)[0]
    }
    fun getTransactionParentCategory(content:String): String {
        var delimiter = "\"parentCategory\":{\"data\":"
        var parts = content.split(delimiter)
        if(parts[1].startsWith("null"))return "null"
        delimiter = "id\":\""
        parts = parts[1].split(delimiter)
        delimiter = "\""
        return parts[1].split(delimiter)[0]
    }
    fun getTransactionTags(content:String): List<String> {
        var delimiter = "\"tags\":{\"data\":["
        val delimiter2 = "}],\"links"
        var parts = content.split(delimiter, delimiter2)
        if(parts[1].startsWith("]"))return listOf("null")
        delimiter = "},{"
        parts = parts[1].split(delimiter)
        val tagList = mutableListOf<String>()
        for (i in parts){
            delimiter = "id\":\""
            val tagH= i.split(delimiter)
            delimiter = "\""
            tagList.add(tagH[1].split(delimiter)[0])
        }
        return tagList.toList()
    }

    fun getAccounts():MutableList<Account>{
        return accounts
    }
    fun getTransactions():MutableList<Transaction>{
        return transactions
    }
    fun getCategories():MutableList<Category>{
        return categories
    }
}

@Composable
fun HomeScreen(viewModel: MyViewModel = viewModel()) {
    while(viewModel.getCategories().size==0)Thread.sleep(10)
    val passthrough = Passthrough(viewModel.getAccounts().toList(), viewModel.getTransactions().toList(), viewModel.getCategories().toList(), "")
    LazyColumn() {
        items(viewModel.getAccounts()) { account ->
            HeaderCard(account, passthrough)
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
fun HeaderCard(info:Account, passthrough:Passthrough){
    val passthrough2 = Passthrough(passthrough.accounts, passthrough.transactions, passthrough.categories, info.id)
    Column(){
        var isExpanded by remember { mutableStateOf(false) }
        Row(modifier = Modifier
            .padding(all = 8.dp)
            .clickable { isExpanded = !isExpanded }) {
            Image(
                painter = painterResource(R.drawable.profile_picture),
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
fun CategoryCard(info:Category, passthrough:Passthrough, level:Int){
    val pad = 16+(level*8)

    Column() {
        var isExpanded by remember { mutableStateOf(false) }
        Row(modifier = Modifier
            .padding(all = 8.dp).padding(start = pad.dp)
            .clickable { isExpanded = !isExpanded }) {
            Image(
                painter = painterResource(R.drawable.profile_picture),
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
fun TransactionCard(info:Transaction, passthrough:Passthrough, level:Int){
    val pad = 16+(level*8)
    Column() {
        var isExpanded by remember { mutableStateOf(false) }
        Row(modifier = Modifier
            .padding(all = 8.dp).padding(start=pad.dp)
            .clickable { isExpanded = !isExpanded }) {
            Image(
                painter = painterResource(R.drawable.profile_picture),
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
            painter = painterResource(R.drawable.profile_picture),
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
        HomeScreen()
    }
}