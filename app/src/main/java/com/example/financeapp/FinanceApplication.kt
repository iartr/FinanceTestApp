package com.example.financeapp

import android.app.Application
import com.example.financeapp.di.appModule
import com.example.financeapp.di.stocksFeatureModule
import com.example.financeapp.di.websocketModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class FinanceApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@FinanceApplication)
            modules(websocketModule, appModule, stocksFeatureModule)
        }
    }
}