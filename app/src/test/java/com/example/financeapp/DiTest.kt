package com.example.financeapp

import com.example.financeapp.di.appModule
import com.example.financeapp.di.stocksFeatureModule
import com.example.financeapp.di.websocketModule
import com.example.financeapp.utils.AppScopes
import org.junit.Test
import org.koin.test.KoinTest
import org.koin.test.verify.verify

class DiTest : KoinTest {

    @Test
    fun verifyDi() {
        websocketModule.verify()
        appModule.verify()
        stocksFeatureModule.verify(extraTypes = listOf(AppScopes::class))
    }
}