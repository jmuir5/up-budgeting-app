package com.example.upbudgetapp

import kotlinx.serialization.*


@Serializable
class AccountResponse(
    val data:Array<AccountItem>,
    val links:ObjectLinks
)

@Serializable
class TransactionResponse(
    val data:Array<TransactionItem>,
    val links:ObjectLinks
)


@Serializable
class AccountItem(
    val type:String,
    val id:String,
    val attributes:AccountAttributes,
    val relationships:AccountRelationships,
    val links:SelfLinks?=null
)

@Serializable
class TransactionItem(
    val type:String,
    val id:String,
    val attributes:TransactionAttributes,
    val relationships:TransactionRelationships,
    val links:SelfLinks?=null
)

@Serializable
class AccountAttributes(
    val displayName:String,
    val accountType:AccountTypeEnum,
    val ownershipType:OwnershipTypeEnum,
    val balance:MoneyObject,
    val createdAt:String,
)

@Serializable
class TransactionAttributes(
    val status:TransactionStatusEnum,
    val rawText:String?=null,
    val description:String,
    val message:String?=null,
    val isCategorizable:Boolean,
    val holdInfo:HoldInfoObject?=null,
    val roundUp:RoundUpObject?=null,
    val cashback:CashBackObject?=null,
    val amount:MoneyObject,
    val foreignAmount:MoneyObject?=null,
    val cardPurchaseMethod:CardPurchaseObject?=null,
    val settledAt:String?=null,
    val createdAt:String
)

@Serializable
enum class AccountTypeEnum{
    SAVER, TRANSACTIONAL, HOME_LOAN
}

@Serializable
enum class OwnershipTypeEnum{
    INDIVIDUAL, JOINT
}

@Serializable
enum class TransactionStatusEnum{
    HELD, SETTLED
}

@Serializable
class HoldInfoObject(
    val amount:MoneyObject,
    val foreignAmount: MoneyObject?=null
)

@Serializable
class RoundUpObject(
    val amount: MoneyObject,
    val boostPortion:MoneyObject
)

@Serializable
class CashBackObject(
    val description:String,
    val amount:MoneyObject
)

@Serializable
class CardPurchaseObject(
    val method:CardPurchaseEnum,
    val cardNumberSuffix:String?=null
)

@Serializable
enum class CardPurchaseEnum{
    BAR_CODE, OCR, CARD_PIN, CARD_DETAILS, CARD_ON_FILE, ECOMMERCE, MAGNETIC_STRIPE, CONTACTLESS
}

@Serializable
class AccountRelationships(
    val transactions:AccTransactions
)


@Serializable
class TransactionRelationships(
    val account:ParentAccountObject,
    val transferAccount:TransferAccountObject,
    val category:Category?=null,
    val parentCategory:ParentCategory,
    val tags:Tags
)

@Serializable
class AccTransactions(
    val links:AccTransacLinks?=null
)

@Serializable
class AccTransacLinks(
    val related:String
)

@Serializable
class ParentAccountObject(
    val data:ParentAccountData,
    val links:RelLinks?=null
)

@Serializable
class ParentAccountData(
    val type:String,
    val id:String
)

@Serializable
class TransferAccountObject(
    val data:TransferAccountData?=null,
    val links:SelfLinks?=null
)

@Serializable
class TransferAccountData(
    val type:String,
    val id:String
)

@Serializable
class Category(
    val data:CategoryData?=null,
    val links:SelfRelLinks?=null
)

@Serializable
class CategoryData(
    val type:String,
    val id:String
)

@Serializable
class ParentCategory(
    val data:CategoryData?=null,
    val links:RelLinks?=null
)

@Serializable
class Tags(
    val data:Array<Tag>,
    val links:TagLinks
)

@Serializable 
class Tag(
    val type:String,
    val id:String
)

@Serializable 
class TagLinks(
    val self:String
)

@Serializable 
class MoneyObject(
    val currencyCode:String,
    val value:String,
    val valueInBaseUnits:Long
)

@Serializable 
class SelfLinks(
    val self:String
)

@Serializable  
class SelfRelLinks(
    val self:String,
    val related:String?=null
)

@Serializable 
class RelLinks(
    val related:String
)

@Serializable
class ObjectLinks(
    val prev:String?=null,
    val next:String?=null
)