package com.example.upbudgetapp

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.upbudgetapp.api.RetrofitHelper
import com.example.upbudgetapp.api.UpApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class MainViewModel: ViewModel {
    constructor(passedKey: String) : super() {
        viewModelScope.launch {
            // Coroutine that will be canceled when the ViewModel is cleared.
            massPopulate(passedKey)
        }
    }
    private var accounts:MutableList<Account> = mutableListOf()
    private var transactions:MutableList<Transaction> = mutableListOf()
    private var categories:MutableList<Category> = mutableListOf()
    private val upApi: UpApi by lazy {
        RetrofitHelper.instance
    }
    private var responseCode=0


    suspend fun massPopulate(passedKey:String) = withContext(Dispatchers.Default){
        populateAccounts(passedKey) // old method, works fine
        //populateAccounts2(passedKey) //retrofit method - doesnt work by its self
        populateTransactions(passedKey) //old method, no longer needed, not going to delete till retrofit is 100%
        //populateTransactions2(passedKey) //retrofit method, works
        populateCategories()
    }
    suspend fun populateAccounts(passedKey:String) = withContext(Dispatchers.Default) {
        // Heavy work
        Log.d("passed Key accounts ", passedKey)
        val getAccount = URL("https://api.up.com.au/api/v1/accounts")
        val http: HttpURLConnection = getAccount.openConnection() as HttpURLConnection
        http.setRequestProperty(
            "Authorization",
            "Bearer $passedKey"//up:yeah:QFGul2kGiQLYl97cT2LzvncSL5tsCdNvaDOoLmrbW9uWUmxF0uwAYl77atL5CT3cuZed8qcTKIhaI6nTrM1Jax1Vab2U86yzoqJeWwgqOOEhEY5QtHZj8k206TfbvNi3"
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
            Log.d("XXXapi result: ", parts.toString())

            for(i in parts) {
                if (i.startsWith("\"id\":")) {
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

    suspend fun populateAccounts2(passedKey:String) = withContext(Dispatchers.Default){
        val map = HashMap<String, String>()
        map["Authorization"] = passedKey
        val accountsx = upApi.getAccounts()
        val delimiter = "}, {type=accounts,"
        val delimiter2 = "data=[{type=accounts,"
        val delimiter3 = "}}],"
        val parts = accountsx.body().toString().split(delimiter, delimiter2, delimiter3)
        for(i in parts) {
            if (i.startsWith(" id=")) {
                accounts.add(
                    Account(
                        getId2(i),
                        getAccountName2(i),
                        getAccountBalance2(i),
                        getAccountCurrency2(i)
                    )
                )
                Log.e("processed accounts2", accounts.toString())
                Log.e("processed accounts2", accounts.size.toString())
            }
        }

    }
    suspend fun populateTransactions(passedKey:String) = withContext(Dispatchers.Default) {
        // Heavy work
        val getAccount = URL("https://api.up.com.au/api/v1/transactions")
        val http: HttpURLConnection = getAccount.openConnection() as HttpURLConnection
        http.setRequestProperty(
            "Authorization",
            "Bearer "+passedKey//up:yeah:QFGul2kGiQLYl97cT2LzvncSL5tsCdNvaDOoLmrbW9uWUmxF0uwAYl77atL5CT3cuZed8qcTKIhaI6nTrM1Jax1Vab2U86yzoqJeWwgqOOEhEY5QtHZj8k206TfbvNi3"
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
        }finally {
            http.disconnect()
        }
    }

    suspend fun populateTransactions2(passedKey:String) = withContext(Dispatchers.Default) {
        // Heavy work
        val map = HashMap<String, String>()
        map["Authorization"] = "Bearer "+passedKey//"Bearer up:yeah:QFGul2kGiQLYl97cT2LzvncSL5tsCdNvaDOoLmrbW9uWUmxF0uwAYl77atL5CT3cuZed8qcTKIhaI6nTrM1Jax1Vab2U86yzoqJeWwgqOOEhEY5QtHZj8k206TfbvNi3"
        val transactionsx = upApi.getTransactions()
        val delimiter = "}, {type=transactions,"
        val delimiter2 = "data=[{type=transactions,"
        val delimiter3 = "}}],"
        val parts = transactionsx.body().toString().split(delimiter, delimiter2, delimiter3)
        for(i in parts) {
            if(i.startsWith(" id=")) {
                transactions.add(
                    Transaction(
                        getId2(i),
                        getTransactionAmount2(i),
                        getTransactionCurrency2(i),
                        getTransactionPayee2(i),
                        getTransactionPayeeL2(i),
                        getTransactionDesc2(i),
                        getAccountId2(i),
                        getTransactionCategory2(i),
                        getTransactionParentCategory2(i),
                        getTransactionTags2(i)
                    )
                )
            }
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

    //get account functions for old method, kept untill retrofit is reliable or serialisation is implemented
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

    //get account functions for retrofit
    fun getId2(content:String): String {
        var delimiter = "id="
        val parts = content.split(delimiter)
        delimiter = ","

        return parts[1].split(delimiter)[0]
    }
    fun getAccountName2(content:String): String {
        var delimiter = "displayName="
        val parts = content.split(delimiter)
        delimiter = ","

        return parts[1].split(delimiter)[0]
    }
    fun getAccountBalance2(content:String): Float {
        var delimiter = "valueInBaseUnits="
        val parts = content.split(delimiter)
        delimiter = ".0}"
        return parts[1].split(delimiter)[0].toFloat()
    }
    fun getAccountCurrency2(content:String): String {
        var delimiter = "currencyCode="
        val parts = content.split(delimiter)
        delimiter = ","

        return parts[1].split(delimiter)[0]
    }

    //get transaction functions for old method, kept untill retrofit is reliable or serialisation is implemented
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
        delimiter = "message\":"
        parts = parts[1].split(delimiter)
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

    //get transaction functions for retrofit
    fun getTransactionAmount2(content:String): Float {
        var delimiter = "amount="
        var parts = content.split(delimiter)
        delimiter = "valueInBaseUnits="
        parts = parts[1].split(delimiter)
        delimiter = ".0}"
        return parts[1].split(delimiter)[0].toFloat()
    }
    fun getTransactionCurrency2(content:String): String {
        var delimiter = "amount="
        var parts = content.split(delimiter)
        delimiter = "currencyCode="
        parts = parts[1].split(delimiter)
        delimiter = ","
        return parts[1].split(delimiter)[0]
    }
    fun getTransactionPayee2(content:String): String {
        var delimiter = "attributes={"
        var parts = content.split(delimiter)
        delimiter = "description="
        parts = parts[1].split(delimiter)
        if(parts[1].startsWith("null"))return "null"
        delimiter = ","
        return parts[1].split(delimiter)[0]
    }
    fun getTransactionPayeeL2(content:String): String {
        var delimiter = "attributes={"
        var parts = content.split(delimiter)
        delimiter = "rawText="
        parts = parts[1].split(delimiter)
        if(parts[1].startsWith("null"))return "null"
        delimiter = ","
        return parts[1].split(delimiter)[0]
    }
    fun getTransactionDesc2(content:String): String {
        var delimiter = "attributes={"
        var parts = content.split(delimiter)
        //for(i in parts)Log.e("desc", i)
        delimiter = "message="
        parts = parts[1].split(delimiter)
        //for(i in parts)Log.e("transaction", i)
        if(parts[1].startsWith("null"))return "null"
        delimiter = ","
        return parts[1].split(delimiter)[0]
    }
    fun getAccountId2(content:String): String {
        var delimiter = "account="
        var parts = content.split(delimiter)
        delimiter = "id="
        parts = parts[1].split(delimiter)
        delimiter = "}"
        return parts[1].split(delimiter)[0]
    }
    fun getTransactionCategory2(content:String): String {
        var delimiter = "category={data="
        var parts = content.split(delimiter)
        if(parts[1].startsWith("null"))return "null"
        delimiter = "id="
        parts = parts[1].split(delimiter)
        delimiter = "}"
        return parts[1].split(delimiter)[0]
    }
    fun getTransactionParentCategory2(content:String): String {
        var delimiter = "parentCategory={data="
        var parts = content.split(delimiter)
        if(parts[1].startsWith("null"))return "null"
        delimiter = "id="
        parts = parts[1].split(delimiter)
        delimiter = "}"
        return parts[1].split(delimiter)[0]
    }
    fun getTransactionTags2(content:String): List<String> {
        var delimiter = "tags={data=["
        var delimiter2 = "}],links"
        var parts = content.split(delimiter, delimiter2)
        if(parts[1].startsWith("]"))return listOf("null")
        delimiter = "},{"
        parts = parts[1].split(delimiter)
        val tagList = mutableListOf<String>()
        for (i in parts){
            delimiter = "id="
            val tagH= i.split(delimiter)
            delimiter = "}"
            delimiter2 = ","
            tagList.add(tagH[1].split(delimiter, delimiter2)[0])
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