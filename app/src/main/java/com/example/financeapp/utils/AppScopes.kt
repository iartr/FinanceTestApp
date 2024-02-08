package com.example.financeapp.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppScopes {
    val mainDispatcherImmediate: CoroutineDispatcher = Dispatchers.Main.immediate
    val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
    val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

    val globalIoScope: CoroutineScope = newDefaultScope(ioDispatcher)

    fun newDefaultScope(dispatcher: CoroutineDispatcher = ioDispatcher): CoroutineScope {
        return CoroutineScope(
            SupervisorJob() +
                    dispatcher +
                    CoroutineName("coroutine-name-") +
                    CoroutineExceptionHandler { coroutineContext, throwable ->
                        android.util.Log.e("CoroutineError", "An error ocurred in coroutine", throwable)
                    }
        )
    }

    companion object {
        val instance by lazy { AppScopes() }
    }
}