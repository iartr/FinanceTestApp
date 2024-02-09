package com.example.financeapp.presentation

sealed interface MainUiState {
    object Loading : MainUiState

    object Error : MainUiState

    data class Success(val list: List<MainUiData>) : MainUiState
}

data class MainUiData(
    val icon: String?,
    val title: String,
    val subtitle: String,
    val showSubtitle: Boolean,
    val percentChangeFromLastClose: String,
    val lastTradePriceWithChangePoints: String,
    val percentChangeTypoColor: androidx.compose.ui.graphics.Color,
    val showBubble: Boolean,
    val strokeColor: androidx.compose.ui.graphics.Color,
)