package me.tsihen.cdqzScore.util

import kotlinx.coroutines.*
import org.jsoup.Connection

open class AsyncHelper<T>(val scope: CoroutineScope, val action: AsyncHelper<T>.() -> T) {
    private var onSuccess: AsyncHelper<T>.(res: T) -> Unit = {}
    private var onFailed: AsyncHelper<T>.(exception: Throwable) -> Unit = {}
    private var onMsg: AsyncHelper<T>.(msg: String) -> Unit = {}

    fun sendMsg(msg: String) {
        scope.launch(Dispatchers.Main) {
            onMsg(msg)
        }
    }

    fun success(handler: AsyncHelper<T>.(res: T) -> Unit): AsyncHelper<T> {
        onSuccess = handler
        return this
    }

    fun failed(handler: AsyncHelper<T>.(exception: Throwable) -> Unit): AsyncHelper<T> {
        onFailed = handler
        return this
    }

    fun message(handler: AsyncHelper<T>.(msg: String) -> Unit): AsyncHelper<T> {
        onMsg = handler
        return this
    }

    fun startBlocking() {
        try {
            val res = action()
            onSuccess(res)
        } catch (e: Throwable) {
            onFailed(e)
        }
    }

    fun start() {
        scope.launch(Dispatchers.IO) {
            try {
                val res = action()
                withContext(Dispatchers.Main) {
                    onSuccess(res)
                }
            } catch (e: Throwable) {
                withContext(Dispatchers.Main) {
                    onFailed(e)
                }
            }
        }
    }
}