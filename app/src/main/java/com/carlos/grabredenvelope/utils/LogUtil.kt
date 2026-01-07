package com.carlos.grabredenvelope.utils

import android.util.Log
import org.json.JSONObject
import org.json.JSONArray

object LogUtil {

    var DEBUG = true
    private const val DEFAULT_TAG = "LogUtil"

    fun v(msg: String, tag: String = DEFAULT_TAG) { if (DEBUG) Log.v(tag, msg) }
    fun d(msg: String, tag: String = DEFAULT_TAG) { if (DEBUG) Log.d(tag, msg) }
    fun i(msg: String, tag: String = DEFAULT_TAG) { if (DEBUG) Log.i(tag, msg) }
    fun w(msg: String, tag: String = DEFAULT_TAG) { if (DEBUG) Log.w(tag, msg) }
    fun e(msg: String, tag: String = DEFAULT_TAG, throwable: Throwable? = null) { if (DEBUG) Log.e(tag, msg, throwable) }

    fun json(jsonStr: String, tag: String = DEFAULT_TAG) {
        if (!DEBUG) return
        try {
            val json = jsonStr.trim()
            when {
                json.startsWith("{") -> Log.d(tag, JSONObject(json).toString(4))
                json.startsWith("[") -> Log.d(tag, JSONArray(json).toString(4))
                else -> Log.d(tag, jsonStr)
            }
        } catch (e: Exception) {
            Log.e(tag, "JSON格式化失败", e)
        }
    }
}
