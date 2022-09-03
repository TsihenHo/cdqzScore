package me.tsihen.cdqzScore.util

import android.util.Log

private const val LOG_TAG = "成都七中成绩查询"

fun v(msg: String) {
    Log.v("FAKE", msg)
}

fun d(msg: String) {
    Log.d(LOG_TAG, msg)
}

fun i(msg: String) {
    Log.i(LOG_TAG, msg)
}

fun i(e: Throwable) {
    Log.i(LOG_TAG, Log.getStackTraceString(e))
}

fun e(e: Throwable) {
    Log.e(LOG_TAG, Log.getStackTraceString(e))
}