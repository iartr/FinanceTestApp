package com.example.financeapp.presentation

import androidx.lifecycle.ViewModel
import com.example.financeapp.domain.StocksRepository
import com.example.financeapp.utils.AppScopes
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    private val scopes: AppScopes,
    private val stocksRepository: StocksRepository
) : ViewModel() {

    private val uiStateMutable = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = uiStateMutable.asStateFlow()

    private val scope = scopes.newDefaultScope(scopes.ioDispatcher)

    init {
        scope.launch {
            stocksRepository.observeStocks()
                .onStart { uiStateMutable.emit(MainUiState.Loading) }
                .catch { uiStateMutable.emit(MainUiState.Error) }
                .map { domainCollection -> domainCollection.map { Mapper(it).map() } }
                .collectLatest {
                    withContext(scopes.mainDispatcherImmediate) {
                        uiStateMutable.emit(MainUiState.Success(it))
                    }
                }
        }
    }

    fun onTickerClick(uiData: MainUiData) {
        // empty
    }

    fun onRetryClick() {
        scope.launch { 
            stocksRepository.clearAndReconnect()
        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }
}