package com.example.financeapp.domain

data class StockDomain(
    val ticker: String,
    val percentChangeFromLastClose: Double, // Изменение в процентах относительно цены закрытия предыдущей торговой сессии
    val lastTradeExchangeName: String, // Биржа последней сделки
    val paperName: String, // Название бумаги
    val lastTradePrice: Double, // Цена последней сделки
    val priceChangePointsFromLastClose: Double, // Изменение цены последней сделки в пунктах относительно цены закрытия предыдущей торговой сессии
    val icon: String?,
    val changed: Boolean,
    val changedIncrease: Boolean
)
