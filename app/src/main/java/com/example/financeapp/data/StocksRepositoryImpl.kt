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

    private val tickerCache = ConcurrentHashMap<String, StockDomain>(tickers.size)

    init {
        websocketDataSource.observeWebsocketEvents()
            .onEach { Log.i("WebsocketEvent", "$it") }
            .filterIsInstance<WebSocketState.Open>()
            .onEach {
                val toSend = JSONArray()
                    .put("quotes")
                    .put(tickersJsonArray)
                    .toString()

                it.webSocket.send(toSend)
            }
            .launchIn(appScopes.globalIoScope)

        websocketDataSource.observeWebsocketEvents()
            .filterIsInstance<WebSocketState.Failure>()
            .onEach { websocketDataSource.onWebSocketFailure(it.webSocket) }
            .launchIn(appScopes.globalIoScope)
    }

    override suspend fun clear() {
        tickerCache.clear()
        websocketDataSource.onWebSocketFailure(webSocket = null)
    }

    override fun observeStocks(): Flow<Collection<StockDomain>> {
        return flow {
            websocketDataSource.observeWebsocketEvents()
                .filterIsInstance<WebSocketState.Message>()
                .mapNotNull { runCatching { JSONArray(it.text) }.getOrNull() }
                .filter { it.get(0) == "q" }
                .map { it.getJSONObject(1) }
                .filter { !it.optDouble("pcp", Double.NaN).isNaN() }
                .collect { message ->
                    val ticker = message.getString("c")
                    val cached = tickerCache[ticker]

                    val newTickerValue = message.optDouble("pcp", Double.NaN)
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
                                lastTradeExchangeName = message.optString("ltr", cached.lastTradeExchangeName),
                                paperName = message.optString("name", message.optString("name2", cached.paperName)),
                                lastTradePrice = message.optDouble("ltp", cached.lastTradePrice),
                                priceChangePointsFromLastClose = message.optDouble("chg", cached.priceChangePointsFromLastClose),
                                changed = changed,
                                changedIncrease = changedIncrease
                            )
                        } else {
                            it
                        }

                    } ?: StockDomain(
                        ticker = ticker,
                        percentChangeFromLastClose = newTickerValue,
                        lastTradeExchangeName = message.optString("ltr", ""),
                        paperName = message.optString("name", message.optString("name2", "")),
                        lastTradePrice = message.optDouble("ltp", 0.0),
                        priceChangePointsFromLastClose = message.optDouble("chg", 0.0),
                        icon = "https://tradernet.com/logos/get-logo-by-ticker?ticker=$ticker",
                        changed = false,
                        changedIncrease = false
                    )

                    tickerCache[ticker] = updatedStock

                    emit(tickerCache.values)
                }
        }
    }

    private companion object {
        private val tickers = listOf("SP500.IDX", "AAPL.US", "RSTI", "GAZP", "MRKZ", "RUAL", "HYDR", "MRKS", "SBER", "FEES", "TGKA", "VTBR", "ANH.US", "VICL.US", "BURG.US", "NBL.US", "YETI.US", "WSFS.US", "NIO.US", "DXC.US", "MIC.US", "HSBC.US", "EXPN.EU", "GSK.EU", "SHP.EU", "MAN.EU", "DB1.EU", "MUV2.EU", "TATE.EU", "KGF.EU", "MGGT.EU", "SGGD.EU")
        private val tickersJsonArray = JSONArray()
            .apply { tickers.forEach { put(it) } }
    }
}