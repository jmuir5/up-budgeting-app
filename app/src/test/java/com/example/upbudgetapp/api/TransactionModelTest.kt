package com.example.upbudgetapp.api

import com.example.upbudgetapp.TransactionResponse
import kotlinx.serialization.json.Json
import org.junit.Test

class TransactionModelTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `can decode transaction response`() {
        val serializer = ApiTransactionResponse.serializer()

        val response = json.decodeFromString(serializer, JSON_DATA)

        assert(response.data.isNotEmpty())
    }
}

private const val JSON_DATA = """
{
  "data": [
    {
      "type": "transactions",
      "id": "622d1b85-2f8a-4b53-94b6-0a2d3a922474",
      "attributes": {
        "status": "SETTLED",
        "rawText": null,
        "description": "David Taylor",
        "message": "Money for the pizzas last night.",
        "isCategorizable": true,
        "holdInfo": null,
        "roundUp": null,
        "cashback": null,
        "amount": {
          "currencyCode": "AUD",
          "value": "-59.98",
          "valueInBaseUnits": -5998
        },
        "foreignAmount": null,
        "cardPurchaseMethod": null,
        "settledAt": "2023-01-23T03:05:03+11:00",
        "createdAt": "2023-01-23T03:05:03+11:00"
      },
      "relationships": {
        "account": {
          "data": {
            "type": "accounts",
            "id": "6222b223-6885-4e0e-8942-c07ffadd0226"
          },
          "links": {
            "related": "https://api.up.com.au/api/v1/accounts/6222b223-6885-4e0e-8942-c07ffadd0226"
          }
        },
        "transferAccount": {
          "data": null
        },
        "category": {
          "data": null,
          "links": {
            "self": "https://api.up.com.au/api/v1/transactions/622d1b85-2f8a-4b53-94b6-0a2d3a922474/relationships/category"
          }
        },
        "parentCategory": {
          "data": null
        },
        "tags": {
          "data": [
            {
              "type": "tags",
              "id": "Pizza Night"
            }
          ],
          "links": {
            "self": "https://api.up.com.au/api/v1/transactions/622d1b85-2f8a-4b53-94b6-0a2d3a922474/relationships/tags"
          }
        }
      },
      "links": {
        "self": "https://api.up.com.au/api/v1/transactions/622d1b85-2f8a-4b53-94b6-0a2d3a922474"
      }
    }
  ],
  "links": {
    "prev": null,
    "next": null
  }
}
"""