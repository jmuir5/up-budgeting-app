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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



class MainViewModel: ViewModel {
    constructor(passedKey: String) : super() {
        viewModelScope.launch {
            // Coroutine that will be canceled when the ViewModel is cleared.
            massPopulate(passedKey)
        }
    }
    private var accounts:MutableList<Account> = mutableListOf()
    private var transactions:MutableList<Transaction> = mutableListOf()
    private var categories:MutableList<AppCategory> = mutableListOf()
    private val upApi: UpApi by lazy {
        RetrofitHelper.instance
    }
    private var responseCode=0


    suspend fun massPopulate(passedKey:String) = withContext(Dispatchers.Default){
        populateAccounts() //retrofit+serialisation method, WROKS
        populateTransactions() //retrofit+serialisation method, WORKS
        populateCategories()
    }

    suspend fun populateAccounts() =withContext(Dispatchers.IO){
        Log.e("populate transactions started", "here")
        lateinit var accountsx: AccountResponse
        try {
            accountsx= upApi.getAccounts().body()!!
        }catch (e:Exception){
            Log.e("something went wrong", e.toString())
        }
        if (accountsx != null) {
            for(i in accountsx.data){
                accounts.add(
                    Account(
                        i.id,
                        i.attributes.displayName,
                        i.attributes.balance.valueInBaseUnits,
                        i.attributes.balance.currencyCode,
                    )
                )
            }
        }
    }

    suspend fun populateTransactions() =withContext(Dispatchers.IO){
        Log.e("populate transactions started", "here")
        var transactionsx=upApi.getTransactions().body()
        if (transactionsx != null) {
            for(i in transactionsx.data){
                val tags = mutableListOf<String>()
                for(j in i.relationships.tags.data){
                    tags.add(j.id)
                }
                transactions.add(
                    Transaction(
                        i.id,
                        i.attributes.amount.valueInBaseUnits,
                        i.attributes.amount.currencyCode,
                        i.attributes.description,
                        i.attributes.rawText,
                        i.attributes.message,
                        i.relationships.account.data.id,
                        i.relationships.category?.data?.id,
                        i.relationships.parentCategory?.data?.id,
                        tags
                    )
                )
            }
        }
    }

    fun populateCategories(){
        val catNames = mutableListOf<String?>()
        val catAmounts = mutableListOf<Long?>()
        val catId = mutableListOf<MutableList<String>>()
        val catParent = mutableListOf<String?>()
        val catIsParent = mutableListOf<Boolean?>()
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
                    catAmounts[index] = catAmounts[index]?.plus(i.amount)
                    catId[index].add(i.id)
                }
            }
        for(i in transactions){
            if(catNames.contains(i.categoryId)){
                val index = catNames.indexOf(i.categoryId)
                catAmounts[index] = catAmounts[index]?.plus(i.amount)
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
            categories.add(AppCategory(catNames[i], catAmounts[i], catParent[i], catIsParent[i], catId[i]))
        }
    }


    fun getAccounts():MutableList<Account>{
        return accounts
    }
    fun getTransactions():MutableList<Transaction>{
        return transactions
    }
    fun getCategories():MutableList<AppCategory>{
        return categories
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
            return MainViewModel(initKey) as T
        }
    }
}