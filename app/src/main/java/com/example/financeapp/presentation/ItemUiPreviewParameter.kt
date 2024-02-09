package com.example.financeapp.presentation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.example.financeapp.ui.theme.BubbleGreen
import com.example.financeapp.ui.theme.BubbleRed
import com.example.financeapp.ui.theme.TradingGreen
import com.example.financeapp.ui.theme.TradingRed

class ItemUiPreviewParameter : PreviewParameterProvider<MainUiData> {
    override val values: Sequence<MainUiData> = sequenceOf(
        baseUiItem.copy(showSubtitle = false, percentChangeFromLastClose = "+2%", showBubble = true, strokeColor = BubbleGreen),
        baseUiItem.copy(percentChangeFromLastClose = "-2%", showBubble = true, strokeColor = BubbleRed),
        baseUiItem,
        baseUiItem.copy(showSubtitle = false),
        baseUiItem.copy(percentChangeFromLastClose = "-2%", percentChangeTypoColor = TradingRed),
    )

    companion object {
        val baseUiItem = MainUiData(
            icon = null,
            title = "AnyTitle",
            subtitle = "MCX | SBER",
            showSubtitle = true,
            percentChangeFromLastClose = "+2.5%",
            lastTradePriceWithChangePoints = "0.123 (1.123)",
            percentChangeTypoColor = TradingGreen,
            showBubble = false,
            strokeColor = Color.Black,
        )
    }
}