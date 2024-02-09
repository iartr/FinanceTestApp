package com.example.financeapp.di

import com.example.financeapp.data.BaseWebSocketListener
import com.example.financeapp.data.StocksRepositoryImpl
import com.example.financeapp.data.WebSocketHolder
import com.example.financeapp.data.WebsocketDataSource
import com.example.financeapp.domain.StocksRepository
import com.example.financeapp.presentation.MainViewModel
import com.example.financeapp.utils.AppScopes
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { AppScopes() }
}

val stocksFeatureModule = module {
    viewModel<MainViewModel> {
        MainViewModel(
            scopes = get(),
            stocksRepository = get()
        )
    }

    single<StocksRepository> {
        StocksRepositoryImpl(
            appScopes = get(),
            websocketDataSource = get()
        )
    }
}

val websocketModule = module {
    single<OkHttpClient> {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()
    }

    single<BaseWebSocketListener> { BaseWebSocketListener() }
    single<WebSocketHolder> { WebSocketHolder(get(), get()) }
    single<WebsocketDataSource> {
        WebsocketDataSource(
            webSocketListener = get(),
            webSocketHolder = get()
        )
    }
}