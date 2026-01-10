package com.carlos.grabredenvelope.db

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "wechat_red_envelope")
class WechatRedEnvelope {
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null

    // 创建时间
    var time: Long = System.currentTimeMillis()

    // 红包数量（或描述）
    var count: String = ""
}
