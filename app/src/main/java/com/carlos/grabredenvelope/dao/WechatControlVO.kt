package com.carlos.grabredenvelope.dao

import kotlinx.serialization.Serializable

/**
 * Github: https://github.com/xbdcc/.
 * Created by Carlos on 2019/2/21.
 */
@Serializable
data class WechatControlVO(
    @Transient
    var isMonitorNotification: Boolean = true, //是否监控通知
    var isMonitorChat: Boolean = true, //是否监控聊天列表页面
    var ifGrabSelf: Boolean = true, //是否抢自己发的红包
    @Transient
    var delayOpenTime: Int = 5,
    @Transient
    var delayCloseTime: Int = 10,
    @Transient
    var isCustomClick: Boolean = false,
    var pointX: Long = 0,
    var pointY: Long = 0,

    @Transient
    var isCustomListClick: Boolean = false,
    var listPointX: Long = 0,
    var listPointY: Long = 0,

    )