package com.carlos.grabredenvelope.services.wechat

import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.EditText
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ShellUtils
import com.carlos.cutils.extend.clickFirstNodeInfo
import com.carlos.cutils.extend.findAndClickFirstNodeInfoByViewId
import com.carlos.cutils.extend.findAndClickFirstNodeInfoByViewIdContainsText
import com.carlos.cutils.extend.getNodeInfosByViewId
import com.carlos.cutils.util.AppUtils
import com.carlos.grabredenvelope.data.RedEnvelopePreferences
import com.carlos.grabredenvelope.db.WechatRedEnvelope
import com.carlos.grabredenvelope.db.WechatRedEnvelopeDb
import com.carlos.grabredenvelope.services.BaseAccessibilityService
import com.carlos.grabredenvelope.services.wechat.WechatConstants.RED_ENVELOPE_BEEN_GRAB_ID
import com.carlos.grabredenvelope.services.wechat.WechatConstants.RED_ENVELOPE_CLOSE_ID
import com.carlos.grabredenvelope.services.wechat.WechatConstants.RED_ENVELOPE_COUNT_ID
import com.carlos.grabredenvelope.services.wechat.WechatConstants.RED_ENVELOPE_FLAG_ID
import com.carlos.grabredenvelope.services.wechat.WechatConstants.RED_ENVELOPE_ID
import com.carlos.grabredenvelope.services.wechat.WechatConstants.RED_ENVELOPE_OPEN_ID
import com.carlos.grabredenvelope.services.wechat.WechatConstants.RED_ENVELOPE_RECT_TITLE_ID
import com.carlos.grabredenvelope.services.wechat.WechatConstants.RED_ENVELOPE_TITLE
import com.carlos.grabredenvelope.services.wechat.WechatConstants.RED_ENVELOPE_TITLE_ID
import com.carlos.grabredenvelope.services.wechat.WechatConstants.WECHAT_LUCKYMONEYDETAILUI_ACTIVITY
import com.carlos.grabredenvelope.services.wechat.WechatConstants.WECHAT_LUCKYMONEY_ACTIVITY
import com.carlos.grabredenvelope.services.wechat.WechatConstants.WECHAT_LUCKYMONEY_ACTIVITY1
import com.carlos.grabredenvelope.services.wechat.WechatConstants.WECHAT_PACKAGE
import com.carlos.grabredenvelope.websocket.GsonHelper
import com.carlos.grabredenvelope.websocket.ICommandService
import com.carlos.grabredenvelope.websocket.WebSend
import com.carlos.grabredenvelope.websocket.WsClient
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.IOException


class WechatService : BaseAccessibilityService(), ICommandService {

    override var monitorPackageName = WECHAT_PACKAGE
    override var notificationTitle = RED_ENVELOPE_TITLE

    private var emojitext = ""

    @Volatile
    private var emojiTimes = 0

    @Volatile
    private var emojiInterval = 0

    @Volatile
    private var emojiCount = 0

    @Volatile
    private var canSendEmoji = true

    //WebSocket Client
    private var ws: WsClient? = null


    override fun onCreate() {
        super.onCreate()
        WechatConstants.setVersion(AppUtils.getVersionName(WECHAT_PACKAGE) ?: "")
        loadEmojiConfig()
        canSendEmoji = true

        LogUtils.d("✅ WebSocket onServiceConnected")
        // 启动 WebSocket Client
        ws = WsClient(this)
        ws?.connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        ws?.close()
    }

    //=======================================================================


    override fun handleWsCommand(jsonStr: String) {

    }


    override fun sendWsMessage(jsonStr: String) {
        LogUtils.d("➡️ 已通知 PC: $jsonStr")
        ws?.send(jsonStr)
    }


    //=======================================================================


    /**
     * 部分手机特殊场景下偶现出现红包框但是不走TYPE_WINDOW_STATE_CHANGED的情况，导致不会点击开，手动在点击后调一次避免此问题
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        super.onAccessibilityEvent(event)
        if (AccessibilityEvent.TYPE_VIEW_CLICKED == event.eventType) {
            LogUtils.d("monitorViewClicked:$event")
            if ((status != HAS_CLICKED)) return
            openRedEnvelope(event)
        }
    }

    override fun monitorNotificationChanged(event: AccessibilityEvent) {
        LogUtils.d("monitorNotificationChanged:$event")
        if (RedEnvelopePreferences.wechatControl.isMonitorNotification.not()) {
            return
        }
        if (status == HAS_RECEIVED) {
            return
        }

        super.monitorNotificationChanged(event)
    }

    override fun monitorWindowChanged(event: AccessibilityEvent) {
        LogUtils.d("monitorWindowChanged:$event")

        if (WechatFilter.isRemarkFilter(rootInActiveWindow)) return

        openRedEnvelope(event)
        quitEnvelope(event)
    }

    override fun monitorContentChanged(event: AccessibilityEvent) {
        LogUtils.d("monitorContentChanged:$event")

        if (WechatFilter.isRemarkFilter(rootInActiveWindow)) return

        GlobalScope.launch {
            delay(300L)
            grabRedEnvelope()
        }
        monitorChat()
    }


    /**
     * 监控微信聊天列表页面是否有红包，经测试若聊天页面与通知同时开启聊天页面比通知先监听到，聊天列表已点击的情况下就不用去点击通知栏
     */
    private fun monitorChat() {
        // 监控关闭则不执行后续操作
        if (RedEnvelopePreferences.wechatControl.isMonitorChat.not()) {
            return
        }
        if (findAndClickFirstNodeInfoByViewIdContainsText(
                RED_ENVELOPE_RECT_TITLE_ID, RED_ENVELOPE_TITLE_ID, RED_ENVELOPE_TITLE
            )
        ) {
            status = HAS_RECEIVED
            LogUtils.d("received a redenvelope.")
        }
    }

    /**
     * 对话页面监控点击红包, 从最下面开始点起
     */
    private fun grabRedEnvelope() {
        /* 发现红包点击进入领取红包页面 */
        if (RedEnvelopePreferences.wechatControl.isCustomListClick) {
            LogUtils.d("grabRedEnvelopeCustom")
            grabRedEnvelopeCustom()
        } else {
            LogUtils.d("grabRedEnvelopeAuto")
            grabRedEnvelopeAuto()
        }
    }

    /**
     * 对话页面监控点击红包, 从最下面开始点起
     */
    private fun grabRedEnvelopeAuto() {
        /* 发现红包点击进入领取红包页面 */
        val ifGrabSelf = RedEnvelopePreferences.wechatControl.ifGrabSelf
        if (findAndClickFirstNodeInfoByViewId(
                viewId = RED_ENVELOPE_ID,
                childExistId = RED_ENVELOPE_FLAG_ID,
                childNotExistIds = RED_ENVELOPE_BEEN_GRAB_ID,
                isJustClickLeft = !ifGrabSelf,
                isReverse = true,
                callback = {

                }
            )
        ) {

            // TODO: adb 点击聊天红包框
            val randomX = (400..600).random()
            val randomY = (1880..2000).random()
            sendWsMessage(
                GsonHelper.gson.toJson(
                    WebSend(
                        action = "command",
                        info = "shell input tap $randomX $randomY"
                    )
                )
            )

            status = HAS_CLICKED
            LogUtils.d("received a redenvelope and click.")
        }
    }

    /**
     * 对话页面监控点击红包, 从最下面开始点起
     */
    private fun grabRedEnvelopeCustom() {
        val delayTime = 200L + (1000L * RedEnvelopePreferences.wechatControl.delayOpenTime / 10)
        val pointX = RedEnvelopePreferences.wechatControl.listPointX.toFloat()
        val pointY = RedEnvelopePreferences.wechatControl.listPointY.toFloat()

        LogUtils.d("received delay custom open time:$delayTime")
        executeAdbCommandClick(pointX, pointY, delayTime)

        status = HAS_CLICKED
        LogUtils.d("received a redenvelope and click.")
    }

    /**
     * 拆开红包
     */
    private fun openRedEnvelope(event: AccessibilityEvent) {
        // 进入红包页面点击开按钮
        if (RedEnvelopePreferences.wechatControl.isCustomClick) {
            LogUtils.d("openRedEnvelopeCustom:" + event.className)
            openRedEnvelopeCustom(event)
        } else {
            LogUtils.d("openRedEnvelopeAuto:" + event.className)
            openRedEnvelopeAuto(event)
        }
    }

    /**
     * 自动点击开按钮，高版本系统微信加载有过程查找开按钮不会马上找到，加入延迟防止不自动点击开按钮
     */
    private fun openRedEnvelopeAuto(event: AccessibilityEvent) {
        // 如果当前不在聊天不是微信红包弹框或者已经没执行点击红包操作，则不执行拆的操作
        if ((!isRedEnvelopeDialog(event.className)) or (status != HAS_CLICKED)) {
            return
        }

        GlobalScope.launch {
            LogUtils.d("start find open id")
            val envelopes = getNodeInfosByViewId(RED_ENVELOPE_OPEN_ID, 300, 5)
            LogUtils.d("end find open id")
            if (envelopes.isNullOrEmpty()) {
                // 没有开按钮，则点击退出按钮
                findAndClickFirstNodeInfoByViewId(RED_ENVELOPE_CLOSE_ID, true)
                LogUtils.d("not has open button:$envelopes")
                return@launch
            }

            val delayTime =
                500L + (1000L * RedEnvelopePreferences.wechatControl.delayOpenTime / 10)
            LogUtils.d("delay open time:$delayTime")
            delay(delayTime)
            clickFirstNodeInfo(envelopes, true)

            // TODO: adb 点击红包弹窗按钮
            val randomX = (440..630).random()
            val randomY = (1470..1600).random()
            sendWsMessage(
                GsonHelper.gson.toJson(
                    WebSend(
                        action = "command",
                        info = "shell input tap $randomX $randomY"
                    )
                )
            )

            status = HAS_OPENED
            LogUtils.d("opened a redenvelope")


            val delayTimeClose = (1000L * RedEnvelopePreferences.wechatControl.delayCloseTime / 10)
            LogUtils.d("delay close time:$delayTimeClose")
            if (delayTimeClose != 101000L) {
                val backDelayTime = delayTimeClose + 3000//添加延迟，当做打开页面时长
                delay(backDelayTime)
                //back()
                // TODO: adb 点击返回键
                sendWsMessage(
                    GsonHelper.gson.toJson(
                        WebSend(
                            action = "command",
                            info = "shell input keyevent 4"
                        )
                    )
                )
                LogUtils.d("quit redenvelope detail page.")
            }
            status = WAIT_NEW
        }
    }

    /**
     * Android7.0以上有效，坐标点点击开按钮
     */
    private fun openRedEnvelopeCustom(event: AccessibilityEvent) {
        // 如果当前不在聊天不是微信红包弹框或者已经没执行点击红包操作，则不执行拆的操作
        if ((!isRedEnvelopeDialog(event.className)) or (status != HAS_CLICKED)) {
            return
        }

        val delayTime = 500L + (1000L * RedEnvelopePreferences.wechatControl.delayOpenTime / 10)
        val pointX = RedEnvelopePreferences.wechatControl.pointX.toFloat()
        val pointY = RedEnvelopePreferences.wechatControl.pointY.toFloat()

//        val path = Path()
//        path.moveTo(pointX, pointY)
//        LogUtils.d("delay custom open time:$delayTime")
//        gesturePath(path, delayTime, interval = 500, times = 3)

        LogUtils.d("delay custom open time:$delayTime")
        executeAdbCommandClick(pointX, pointY, delayTime)

        status = HAS_OPENED
        LogUtils.d("opened a redenvelope")
    }

    /**
     * 退出红包详情页
     */
    private fun quitEnvelope(event: AccessibilityEvent) {
        // 如果当前页面不是红包详情页或者没有点开过拆按钮，则不执行退出操作
        if ((!isRedEnvelopeDetail(event.className)) or (status != HAS_OPENED)) {
            return
        }

        GlobalScope.launch {
            saveData()
            val delayTime = (1000L * RedEnvelopePreferences.wechatControl.delayCloseTime / 10)
            LogUtils.d("delay close time:$delayTime")
            if (delayTime != 101000L) {
                delay(delayTime)
//                back()
                // TODO: adb 点击返回键
                sendWsMessage(
                    GsonHelper.gson.toJson(
                        WebSend(
                            action = "command",
                            info = "shell input keyevent 4"
                        )
                    )
                )
            }

            if (canSendEmoji) {
                val emojiState = RedEnvelopePreferences.emojiState
                if (emojiState) {
                    delay(1000L)
                    loadEmojiConfig()
                    sendMessage()
                }
            }
        }
        status = WAIT_NEW
        LogUtils.d("quit redenvelope detail page.")
    }

    /**
     * 是否为红包弹窗
     */
    private fun isRedEnvelopeDialog(className: CharSequence?): Boolean {
        return className == WECHAT_LUCKYMONEY_ACTIVITY || className == WECHAT_LUCKYMONEY_ACTIVITY1
    }

    /**
     * 是否为红包结果页
     */
    private fun isRedEnvelopeDetail(className: CharSequence?): Boolean {
        return className == WECHAT_LUCKYMONEYDETAILUI_ACTIVITY
    }


    /**
     * 记录抢到的金额本地查看记录
     */
    private fun saveData() {
        getNodeInfosByViewId(RED_ENVELOPE_COUNT_ID)?.let {
            if (it.isNullOrEmpty()) return
            val wechatRedEnvelope = WechatRedEnvelope()
            wechatRedEnvelope.count = it[0].text.toString()
            WechatRedEnvelopeDb.insertData(wechatRedEnvelope)
        }
    }


    //--表情 ------------------------------------------------------------------------------------


    private fun loadEmojiConfig() {
        emojitext = RedEnvelopePreferences.autoText
        emojiTimes = RedEnvelopePreferences.emojiTimes
        emojiInterval = RedEnvelopePreferences.emojiInterval
        emojiCount = 0
        LogUtils.d("text:$emojitext")
        LogUtils.d("times:$emojiTimes")
        LogUtils.d("interval:$emojiInterval")
        LogUtils.d("count:$emojiCount")
    }

    /**
     * 找到文本框输入表情，找到发送按钮点击循环执行
     */
    private fun sendMessage() {
        if (emojiCount >= emojiTimes && emojiTimes != 0) {
            emojiCount = 0
            canSendEmoji = true
            return
        }
        if (emojitext.isEmpty()) {
            return
        }
        LogUtils.d("count:$emojiCount")

        val accessibilityNodeInfo = getNodeInfosByViewId(WechatConstants.CHAT_EDITTEXT_ID) ?: return

        for (editText in accessibilityNodeInfo) {
            if (editText.className == EditText::class.java.name) {
                val arguments = Bundle()
                arguments.putCharSequence(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
                    emojitext
                )
                editText.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)

                findAndClickFirstNodeInfoByViewId(WechatConstants.SEND_TEXT_ID)
                LogUtils.d("send a message")

                canSendEmoji = false

                emojiCount++
                GlobalScope.launch {
                    delay(emojiInterval.toLong())
                    sendMessage()
                }
            }
        }
    }


    /**
     * 通过adb命令点击
     */
    private fun executeAdbCommandClick(x: Float, y: Float, delayTime: Long) {
        GlobalScope.launch {
            delay(delayTime)
            try {
//                val command = "adb shell input tap $x $y"
//                val command = "adb shell input tap ${x.toInt()} ${y.toInt()}"
                val command = "input tap ${x.toInt()} ${y.toInt()}"

                val result = ShellUtils.execCmd(command, false, true)
                LogUtils.d("result:$result")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

}