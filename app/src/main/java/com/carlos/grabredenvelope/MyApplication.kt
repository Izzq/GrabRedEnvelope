package com.carlos.grabredenvelope

import androidx.multidex.MultiDexApplication
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import com.carlos.cutils.CUtils
import com.carlos.grabredenvelope.config.AppControlManager
import com.carlos.grabredenvelope.config.Config
import com.tencent.bugly.crashreport.CrashReport


class MyApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        Utils.init(this)
        initLog()

        instance = this

        CUtils.init(this)

        CrashReport.initCrashReport(
            this,
            "4f2daf6e38",
            false
        )

    }

    // init it in ur application
    private fun initLog() {
        val logTag = Config.LogTag
        LogUtils.getConfig()
            .setLogSwitch(true) // 设置 log 总开关，包括输出到控制台和文件，默认开
            .setConsoleSwitch(AppControlManager.isOpenAppLog) // 设置是否输出到控制台开关，默认开
            .setGlobalTag(logTag) // 设置 log 全局标签，默认为空
            // 当全局标签不为空时，我们输出的 log 全部为该 tag，
            // 为空时，如果传入的 tag 为空那就显示类名，否则显示 tag
            .setLogHeadSwitch(false) // 设置 log 头信息开关，默认为开
            .setLog2FileSwitch(true) // 打印 log 时是否存到文件的开关，默认关
//            .setDir(logPath) // 当自定义路径为空时，写入应用的/cache/log/目录中
            .setFilePrefix("") // 当文件前缀为空时，默认为"util"，即写入文件为"util-yyyy-MM-dd$fileExtension"
            .setFileExtension(".log") // 设置日志文件后缀
            .setBorderSwitch(true) // 输出日志是否带边框开关，默认开
            .setSingleTagSwitch(false) // 一条日志仅输出一条，默认开，为美化 AS 3.1 的 Logcat
            .setConsoleFilter(LogUtils.V) // log 的控制台过滤器，和 logcat 过滤器同理，默认 Verbose
            .setFileFilter(Config.LogFileFilterLevel) // log 文件过滤器，和 logcat 过滤器同理，默认 Verbose
            .setStackDeep(2) // log 栈深度，默认为 1
            .setStackOffset(1) // 设置栈偏移，比如二次封装的话就需要设置，默认为 0
            .setSaveDays(3) // 设置日志可保留天数，默认为 -1 表示无限时长
    }

    companion object {
        lateinit var instance: MyApplication
            private set
    }

}