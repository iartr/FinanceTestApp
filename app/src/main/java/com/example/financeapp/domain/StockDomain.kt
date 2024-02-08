package com.example.financeapp.domain

import org.json.JSONObject

data class StockDomain(
    val ticker: String,
    val percentChangeFromLastClose: Double,
    val lastTradeExchangeName: String,
    val paperName: String,
    val lastTradePrice: Double,
    val priceChangePointsFromLastClose: Double,
    val icon: String?,
    val changed: Boolean,
    val changedIncrease: Boolean
) {
    constructor(json: JSONObject) : this(
        ticker = json.getString("c"),
        percentChangeFromLastClose = json.optDouble("pcp", Double.NaN),
        lastTradeExchangeName = json.optString("ltr"),
        paperName = json.optString("name", json.optString("name2")),
        lastTradePrice = json.optDouble("ltp", Double.NaN),
        priceChangePointsFromLastClose = json.optDouble("chg", 0.0),
        icon = "https://tradernet.com/logos/get-logo-by-ticker?ticker=${json.getString("c")}",
        changed = false,
        changedIncrease = false,
    )
}
