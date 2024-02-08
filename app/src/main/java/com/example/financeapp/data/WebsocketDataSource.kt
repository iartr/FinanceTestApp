package com.example.financeapp.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.logging.HttpLoggingInterceptor
import okio.ByteString

class WebsocketDataSource(
    private val webSocketListener: BaseWebSocketListener,
    private val webSocketHolder: WebSocketHolder,
) {
    suspend fun onWebSocketFailure(webSocket: WebSocket?) {
        delay(1500L)
        webSocket?.close(1002, null)
        webSocketHolder.recreateWebSocket()
    }

    fun observeWebsocketEvents(): Flow<WebSocketState> {
        return webSocketListener.observe()
    }
}

sealed class WebSocketState {
    data object JustCreated : WebSocketState()
    data class Open(val webSocket: WebSocket) : WebSocketState()
    data class Message(val webSocket: WebSocket, val text: String) : WebSocketState()
    data class Close(val webSocket: WebSocket, val code: Int, val reason: String) : WebSocketState()
    data class Failure(val webSocket: WebSocket, val throwable: Throwable,) : WebSocketState()
}

class BaseWebSocketListener private constructor(): WebSocketListener() {
    private val stateFlow = MutableStateFlow<WebSocketState>(WebSocketState.JustCreated)

    fun observe(): StateFlow<WebSocketState> = stateFlow.asStateFlow()

    override fun onOpen(webSocket: WebSocket, response: Response) {
        stateFlow.tryEmit(WebSocketState.Open(webSocket))
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        stateFlow.tryEmit(WebSocketState.Message(webSocket, text))
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(1000, null)
        stateFlow.tryEmit(WebSocketState.Close(webSocket, code, reason))
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        stateFlow.tryEmit(WebSocketState.Failure(webSocket, t))
    }

    companion object {
        val instance by lazy { BaseWebSocketListener() }
    }
}

class WebSocketHolder private constructor() {
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()
    }

    @Volatile
    private var webSocket: WebSocket = okHttpClient.newWebSocket(
        Request.Builder().url("wss://wss.tradernet.com").build(),
        BaseWebSocketListener.instance
    )

    private val mutex = Mutex()

    suspend fun recreateWebSocket() {
        mutex.withLock {
            webSocket = okHttpClient.newWebSocket(
                Request.Builder().url("wss://wss.tradernet.com").build(),
                BaseWebSocketListener.instance
            )
        }
    }

    companion object {
        val instance by lazy { WebSocketHolder() }
    }
}