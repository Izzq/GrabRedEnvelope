package com.carlos.grabredenvelope.db

import com.carlos.grabredenvelope.MyApplication


/**
 * Github: https://github.com/xbdcc/.
 * Created by Carlos on 2020-01-22.
 */
object WechatRedEnvelopeDb {


    fun getAllData(): List<WechatRedEnvelope> {
        return AppDatabase.getInstance(MyApplication.instance)
            .wechatRedEnvelopeDao()
            .all
    }

    @Synchronized
    fun insertData(entity: WechatRedEnvelope) {
        AppDatabase.getInstance(MyApplication.instance)
            .wechatRedEnvelopeDao().insert(entity)
    }

}