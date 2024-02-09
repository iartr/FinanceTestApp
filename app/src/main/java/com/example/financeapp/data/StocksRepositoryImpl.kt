package com.example.financeapp.data

import android.util.Log
import com.example.financeapp.domain.StockDomain
import com.example.financeapp.domain.StocksRepository
import com.example.financeapp.utils.AppScopes
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap

class StocksRepositoryImpl(
    private val appScopes: AppScopes,
    private val websocketDataSource: WebsocketDataSource
) : StocksRepository {

    private val tickerCache = ConcurrentHashMap<String, StockDomain>(tickers.size * 2)

    init {
        websocketDataSource.observeWebsocketEvents()
            .onEach { Log.i("WebsocketEvent", "$it") }
            .filterIsInstance<WebSocketState.Open>()
            .onEach { it.webSocket.send(tickersRequest) }
            .launchIn(appScopes.globalIoScope)

        websocketDataSource.observeWebsocketEvents()
            .filterIsInstance<WebSocketState.Failure>()
            .onEach { websocketDataSource.onWebSocketFailure(it.webSocket) }
            .launchIn(appScopes.globalIoScope)
    }

    override suspend fun clearAndReconnect() {
        tickerCache.clear()
        websocketDataSource.onWebSocketFailure(webSocket = null)
    }

    override fun observeStocks(): Flow<Collection<StockDomain>> {
        return flow {
            websocketDataSource.observeWebsocketEvents()
                .filterIsInstance<WebSocketState.Message>()
                .mapNotNull { runCatching { JSONArray(it.text) }.getOrNull() }
                .filter { it.get(0) == "q" }
                .map { StockDomain(it.getJSONObject(1)) }
                .filter { !it.percentChangeFromLastClose.isNaN() }
                .collect { ticker ->
                    val cached = tickerCache[ticker.ticker]

                    val newTickerValue = ticker.percentChangeFromLastClose
                    val updatedStock = cached?.let {
                        var changed = false
                        var changedIncrease = false

                        if (!newTickerValue.isNaN()) {
                            if (newTickerValue != cached.percentChangeFromLastClose) {
                                changed = true
                                changedIncrease = newTickerValue > cached.percentChangeFromLastClose
                            }

                            it.copy(
                                percentChangeFromLastClose = newTickerValue,
                                lastTradeExchangeName = ticker.lastTradeExchangeName.ifBlank { cached.lastTradeExchangeName },
                                paperName = ticker.paperName.ifBlank { cached.paperName },
                                lastTradePrice = ticker.lastTradePrice.takeIf { !it.isNaN() } ?: cached.lastTradePrice,
                                priceChangePointsFromLastClose = ticker.priceChangePointsFromLastClose.takeIf { !it.isNaN() } ?: cached.priceChangePointsFromLastClose,
                                changed = changed,
                                changedIncrease = changedIncrease
                            )
                        } else {
                            it
                        }

                    } ?: ticker

                    tickerCache[ticker.ticker] = updatedStock

                    emit(tickerCache.values)
                }
        }
    }

    private companion object {
        private val tickers = listOf("SP500.IDX", "AAPL.US", "RSTI", "GAZP", "MRKZ", "RUAL", "HYDR", "MRKS", "SBER", "FEES", "TGKA", "VTBR", "ANH.US", "VICL.US", "BURG.US", "NBL.US", "YETI.US", "WSFS.US", "NIO.US", "DXC.US", "MIC.US", "HSBC.US", "EXPN.EU", "GSK.EU", "SHP.EU", "MAN.EU", "DB1.EU", "MUV2.EU", "TATE.EU", "KGF.EU", "MGGT.EU", "SGGD.EU")
        private val tickersJsonArray = JSONArray()
            .apply { tickers.forEach { put(it) } }
        private val tickersRequest = JSONArray()
            .put("quotes")
            .put(tickersJsonArray)
            .toString()
    }
}