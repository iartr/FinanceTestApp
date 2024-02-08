package com.example.financeapp.domain

import kotlinx.coroutines.flow.Flow

interface StocksRepository {
    fun observeStocks(): Flow<Collection<StockDomain>>
    suspend fun clear()
}