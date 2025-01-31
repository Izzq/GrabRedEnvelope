package com.carlos.grabredenvelope.config

import com.carlos.grabredenvelope.BuildConfig

/**
 * Explain:应用开关控制常量
 */
object AppControlManager {


    /**
     * 是否打开应用log
     */
    val isOpenAppLog: Boolean
        get() = BuildConfig.DEBUG


}