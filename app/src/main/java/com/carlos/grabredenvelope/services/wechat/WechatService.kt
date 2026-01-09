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

    //监听的应用包名
    override var monitorPackageName = WECHAT_PACKAGE

    //监听通知文案
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
        initWs()
    }

    override fun onDestroy() {
        super.onDestroy()
        closeWs()
    }

    //=======================================================================
    // WebSocket
    //=======================================================================

    /**
     * 初始化ws
     */
    private fun initWs() {
        LogUtils.d("[ws] ✅ WebSocket onServiceConnected")
        // 启动 WebSocket Client
        ws = WsClient(this)
        ws?.connect()

    }

    /**
     * 关闭ws
     */
    private fun closeWs() {
        ws?.close()
    }


    /**
     * 处理服务端命令
     */
    override fun handleWsCommand(jsonStr: String) {
    }

    /**
     * 发送ws消息给服务端
     */
    override fun sendWsMessage(jsonStr: String) {
        LogUtils.d("[ws] ➡️ 已通知 PC: $jsonStr")
        ws?.send(jsonStr)
    }

    /**
     * 发送点击命令
     */
    private fun sendWsClickCommand(x: Int, y: Int) {
        sendWsMessage(
            GsonHelper.gson.toJson(
                WebSend(
                    action = "command",
                    info = "shell input tap $x $y"
                )
            )
        )
    }

    /**
     * 发送返回键命令
     */
    private fun sendWsBackCommand() {
        sendWsMessage(
            GsonHelper.gson.toJson(
                WebSend(
                    action = "command",
                    info = "shell input keyevent 4"
                )
            )
        )
    }


    //=======================================================================
    // 无障碍事件监听
    //=======================================================================


    /**
     * 部分手机特殊场景下偶现出现红包框但是不走TYPE_WINDOW_STATE_CHANGED的情况，导致不会点击开，手动在点击后调一次避免此问题
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        super.onAccessibilityEvent(event)
        if (AccessibilityEvent.TYPE_VIEW_CLICKED == event.eventType) {
            LogUtils.d("monitorViewClicked:$event")
            if ((status != HAS_CLICKED)) {
                //监视《红包弹窗》，并点击领取按钮
                openRedEnvelope(event)
            }
        }
    }

    /** 监听通知变化 */
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

    /** 监听窗口变化 */
    override fun monitorWindowChanged(event: AccessibilityEvent) {
        LogUtils.d("monitorWindowChanged:$event")

        if (WechatFilter.isRemarkFilter(rootInActiveWindow)) return

        //监视《红包弹窗》，并点击领取按钮
        openRedEnvelope(event)

        //退出《红包详情页》
        quitEnvelope(event, isCheckPage = true)
    }

    /** 监听内容变化 */
    override fun monitorContentChanged(event: AccessibilityEvent) {
        LogUtils.d("monitorContentChanged:$event")

        if (WechatFilter.isRemarkFilter(rootInActiveWindow)) return

        GlobalScope.launch {
            val randomTime = (200L..300L).random()
            delay(randomTime)
            //监视 聊天页面《红包框》
            grabRedEnvelope()
        }

        //监视《微信聊天列表》
        monitorChat()
    }


    //=======================================================================
    // 操作点击
    //=======================================================================


    //上一次点击聊天页面《红包框》时间戳
    private var lastGrabRedEnvelopTimestamp = 0L


    /**
     * 监视《微信聊天列表》页面是否有红包
     *
     * 注意：经测试若聊天页面与通知同时开启聊天页面比通知先监听到，聊天列表已点击的情况下就不用去点击通知栏
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
            LogUtils.d("[envelope] received a red envelope.")
        }
    }

    /**
     * 监视 聊天页面《红包框》, 从最下面开始点起
     */
    private fun grabRedEnvelope() {
        LogUtils.d("grabRedEnvelopeAuto")
        if (System.currentTimeMillis() - lastGrabRedEnvelopTimestamp > 500) {
            lastGrabRedEnvelopTimestamp = System.currentTimeMillis()
            grabRedEnvelopeAuto()
        }
    }

    /**
     * 监视 聊天页面《红包框》，通过无障碍API识别
     */
    private fun grabRedEnvelopeAuto() {
        if (findAndClickFirstNodeInfoByViewId(
                viewId = RED_ENVELOPE_ID,
                childExistId = RED_ENVELOPE_FLAG_ID,
                childNotExistIds = RED_ENVELOPE_BEEN_GRAB_ID,
                isJustClickLeft = !RedEnvelopePreferences.wechatControl.ifGrabSelf,
                isReverse = true,
                callback = {
                }
            )
        ) {
            // TODO: adb 点击 聊天页面《红包框》
            val randomX = (400..600).random()
            var randomY = (1990..2010).random()
            //使用自定义Y坐标
            if (RedEnvelopePreferences.wechatControl.isCustomListClick) {
                val pointY = RedEnvelopePreferences.wechatControl.listPointY.toInt()
                if (pointY > 0) {
                    randomY = (pointY - 10..pointY + 10).random()
                }
            }
            sendWsClickCommand(randomX, randomY)
            status = HAS_CLICKED
            LogUtils.d("[envelope] received a red envelope and click.")
        }
    }


    /**
     * 监视《红包弹窗》，并点击领取按钮
     */
    private fun openRedEnvelope(event: AccessibilityEvent) {
        // 进入红包页面点击开按钮
        LogUtils.d("openRedEnvelopeAuto:" + event.className)
        openRedEnvelopeAuto(event)
    }

    /**
     * 监视《红包弹窗》，并点击领取按钮
     *
     * 注意：高版本系统微信加载有过程查找开按钮不会马上找到，加入延迟防止不自动点击开按钮
     */
    private fun openRedEnvelopeAuto(event: AccessibilityEvent) {

        // 如果当前不在聊天不是微信红包弹框或者已经没执行点击红包操作，则不执行拆的操作
        if ((!isRedEnvelopeDialog(event.className)) or (status != HAS_CLICKED)) {
            return
        }

        GlobalScope.launch {

            LogUtils.d("[envelope] start find open id")
            val envelopes = getNodeInfosByViewId(RED_ENVELOPE_OPEN_ID, 300, 5)
            LogUtils.d("[envelope] end find open id")

            if (envelopes.isNullOrEmpty()) {
                // 没有开按钮，则点击退出按钮，触发无障碍点击节点
                findAndClickFirstNodeInfoByViewId(RED_ENVELOPE_CLOSE_ID, true)
                LogUtils.d("[envelope] not has open button:$envelopes")
                return@launch
            }

            val delayTime = 500L + (1000L * RedEnvelopePreferences.wechatControl.delayOpenTime / 10)
            LogUtils.d("delay open time:$delayTime")
            delay(delayTime)

            //触发无障碍点击节点
            clickFirstNodeInfo(envelopes, true)

            // TODO: adb 点击红包弹窗按钮
            val randomX = (440..630).random()
            var randomY = (1470..1600).random()
            //使用自定义Y坐标
            if (RedEnvelopePreferences.wechatControl.isCustomClick) {
                val pointY = RedEnvelopePreferences.wechatControl.listPointY.toInt()
                if (pointY > 0) {
                    randomY = (pointY - 60..pointY + 60).random()
                }
            }
            sendWsClickCommand(randomX, randomY)

            status = HAS_OPENED
            LogUtils.d("[envelope] opened a red envelope")

            //退出《红包详情页》
            quitEnvelope(event, isCheckPage = false)
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

        LogUtils.d("delay custom open time:$delayTime")
        executeAdbCommandClick(pointX, pointY, delayTime)

        status = HAS_OPENED
        LogUtils.d("[envelope] opened a red envelope")
    }

    /**
     * 退出《红包详情页》
     *
     * @param isCheckPage 是否监测页面信息
     */
    private fun quitEnvelope(event: AccessibilityEvent, isCheckPage: Boolean) {
        //没有点开过拆按钮，则不执行退出操作
        if ((status != HAS_OPENED)) {
            return
        }
        //如果当前页面不是红包详情页，则不执行退出操作
        if (isCheckPage && (!isRedEnvelopeDetail(event.className))) {
            return
        }

        GlobalScope.launch {
            //保存领取信息
            saveData()

            val randomTime = (1500L..3000L).random()
            val delayTime = (randomTime * RedEnvelopePreferences.wechatControl.delayCloseTime / 10)

            LogUtils.d("delay close time:$delayTime")

            if (delayTime != 101000L) {
                delay(delayTime)
                //触发无障碍返回
                //back()
                // TODO: adb 点击返回键
                sendWsBackCommand()
            }

            status = WAIT_NEW
            LogUtils.d("[envelope] quit red envelope detail page.")

            //判断是否发送表情或消息
            if (canSendEmoji) {
                val emojiState = RedEnvelopePreferences.emojiState
                if (emojiState) {
                    delay(1000L)
                    loadEmojiConfig()
                    sendEmojiMessage()
                }
            }
        }
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


    //=======================================================================
    // 表情
    //=======================================================================


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
    private fun sendEmojiMessage() {
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
                    sendEmojiMessage()
                }
            }
        }
    }

}