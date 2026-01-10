package com.carlos.grabredenvelope.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface WechatRedEnvelopeDao {
    @Insert
    fun insert(envelope: WechatRedEnvelope)

    @get:Query("SELECT * FROM wechat_red_envelope ORDER BY time DESC")
    val all: List<WechatRedEnvelope>

    @Query("DELETE FROM wechat_red_envelope")
    fun deleteAll()
}
