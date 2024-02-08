package com.example.financeapp.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.financeapp.data.BaseWebSocketListener
import com.example.financeapp.data.StocksRepositoryImpl
import com.example.financeapp.data.WebSocketHolder
import com.example.financeapp.data.WebsocketDataSource
import com.example.financeapp.domain.StocksRepository
import com.example.financeapp.utils.AppScopes
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    private val scopes: AppScopes,
    private val stocksRepository: StocksRepository
) : ViewModel() {
    sealed class UiState {
        object Loading : UiState()

        object Error : UiState()

        data class Data(
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

        data class ListData(val list: List<Data>) : UiState()
    }

    private val uiStateMutable = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = uiStateMutable.asStateFlow()

    private val scope = scopes.newDefaultScope()

    init {
        scope.launch(scopes.ioDispatcher) {
            stocksRepository.observeStocks()
                .onStart { uiStateMutable.emit(UiState.Loading) }
                .catch { uiStateMutable.emit(UiState.Error) }
                .map { list -> list.map { Mapper(it).map() } }
                .collectLatest {
                    withContext(scopes.mainDispatcherImmediate) {
                        uiStateMutable.emit(UiState.ListData(it))
                    }
                }
        }
    }

    fun onTickerClick() {
        // empty
    }

    fun onRetryClick() {
        scope.launch { 
            stocksRepository.clear() 
        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }

    companion object {
        val factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MainViewModel(
                    scopes = AppScopes.instance,
                    stocksRepository = StocksRepositoryImpl(
                        appScopes = AppScopes.instance,
                        websocketDataSource = WebsocketDataSource(
                            webSocketListener = BaseWebSocketListener.instance,
                            webSocketHolder = WebSocketHolder.instance
                        )
                    )
                )
            }
        }
    }
}