package com.carlos.grabredenvelope.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.lifecycleScope
import com.carlos.cutils.extend.doubleCount
import com.carlos.cutils.extend.getYearToMinute
import com.carlos.grabredenvelope.R
import com.carlos.grabredenvelope.databinding.FragmentRecordBinding
import com.carlos.grabredenvelope.db.WechatRedEnvelopeDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * Github: https://github.com/xbdcc/.
 * Created by Carlos on 2020-01-22.
 */
class RecordFragment : BaseFragment(R.layout.fragment_record) {

    private var list = ArrayList<String>()
    private lateinit var arrayAdapter: ArrayAdapter<String>
    private var startTime = getYearToMinute()
    private var total = 0.0

    private lateinit var binding: FragmentRecordBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRecordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        initData()
    }

    private fun init(view: View) {
        arrayAdapter = ArrayAdapter(
            view.context, R.layout.item_wechat_record, R.id.tv_item_wechat_record, list
        )
        binding.lvWechatRecord.adapter = arrayAdapter
    }

    private fun initData() {
        job = lifecycleScope.launch(Dispatchers.Main) {
            list.clear()
            list.addAll(getData())
            if (list.isNullOrEmpty()) return@launch
            binding.tvRecordTitle.text =
                getString(R.string.history_list_content, startTime, "$total")
            arrayAdapter.notifyDataSetChanged()
        }
    }

    private suspend fun getData(): ArrayList<String> {
        return withContext(Dispatchers.IO) {
            total = 0.0
            val list = ArrayList<String>()
            val wechatRedEnvelopes = WechatRedEnvelopeDb.getAllData()
            for (wechatRedEnvelope in wechatRedEnvelopes.asReversed()) {
                total = total.doubleCount(wechatRedEnvelope.count.split("元")[0].toDouble())
                list.add("${getYearToMinute(wechatRedEnvelope.time)} 助你抢到了 ${wechatRedEnvelope.count}")
            }
            if (wechatRedEnvelopes.isNotEmpty()) {
                startTime = getYearToMinute(wechatRedEnvelopes[0].time)
            }
            return@withContext list
        }
    }

}