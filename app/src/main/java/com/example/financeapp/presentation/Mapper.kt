package com.example.financeapp.presentation

import com.example.financeapp.domain.StockDomain
import com.example.financeapp.ui.theme.BubbleGreen
import com.example.financeapp.ui.theme.BubbleRed
import com.example.financeapp.ui.theme.TradingGreen
import com.example.financeapp.ui.theme.TradingRed

class Mapper(private val domain: StockDomain) {
    fun map(): MainUiData {
        val subtitle = subtitle(domain.lastTradeExchangeName, domain.paperName)
        return MainUiData(
            icon = domain.icon,
            title = domain.ticker,
            subtitle = subtitle,
            showSubtitle = subtitle.isNotBlank(),
            percentChangeFromLastClose = if (domain.percentChangeFromLastClose >= 0.0) "+${domain.percentChangeFromLastClose}%" else "${domain.percentChangeFromLastClose}%",
            lastTradePriceWithChangePoints = "${domain.lastTradePrice} (${domain.priceChangePointsFromLastClose})",
            percentChangeTypoColor = if (domain.percentChangeFromLastClose >= 0.0) TradingGreen else TradingRed,
            showBubble = domain.changed,
            strokeColor = if (domain.changedIncrease) BubbleGreen else BubbleRed
        )
    }

    private fun subtitle(lastTradeExchangeName: String, paperName: String): String {
        return when {
            (lastTradeExchangeName.isNotBlank() && paperName.isNotBlank()) -> "$lastTradeExchangeName | $paperName"
            (lastTradeExchangeName.isNotBlank() && paperName.isBlank()) -> lastTradeExchangeName
            (lastTradeExchangeName.isBlank() && paperName.isNotBlank()) -> paperName
            else -> ""
        }
    }
}