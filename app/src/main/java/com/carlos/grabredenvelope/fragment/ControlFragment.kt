package com.carlos.grabredenvelope.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.blankj.utilcode.util.LogUtils
import com.carlos.grabredenvelope.R
import com.carlos.grabredenvelope.activity.MainActivity
import com.carlos.grabredenvelope.activity.PhonePointActivity
import com.carlos.grabredenvelope.dao.WechatControlVO
import com.carlos.grabredenvelope.data.RedEnvelopePreferences
import com.carlos.grabredenvelope.databinding.FragmentControlBinding
import com.carlos.grabredenvelope.websocket.WebSocketConst
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Github: https://github.com/xbdcc/.
 * Created by 小不点 on 2016/5/27.
 */
class ControlFragment : BaseFragment(R.layout.fragment_control), IMainFragment,
    SeekBar.OnSeekBarChangeListener {


    private var wechatControlVO = WechatControlVO()
    private var t_putong: Int = 0
    private var t_lingqu: Int = 0

    private lateinit var binding: FragmentControlBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentControlBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
    }

    override fun onResume() {
        super.onResume()
        val mainActivity = activity as MainActivity
        updateControlView(mainActivity.checkStatus())
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //获取红包弹窗点击坐标
        if (requestCode == PhonePointActivity.SET_OPEN_RED_POINT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // 获取返回的坐标
            val x = data?.getIntExtra("x", 0) ?: 0
            val y = data?.getIntExtra("y", 0) ?: 0

            // 显示坐标
            binding.etPointX.setText("$x")
            binding.etPointY.setText("$y")

            wechatControlVO.pointX = binding.etPointX.text.toString().toLong()
            wechatControlVO.pointY = binding.etPointY.text.toString().toLong()
            RedEnvelopePreferences.wechatControl = wechatControlVO
        } else if (requestCode == PhonePointActivity.SET_OPEN_LIST_RED_POINT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // 获取返回的坐标
            val x = data?.getIntExtra("x", 0) ?: 0
            val y = data?.getIntExtra("y", 0) ?: 0

            // 显示坐标
            binding.etListPointX.setText("$x")
            binding.etListPointY.setText("$y")

            wechatControlVO.listPointX = binding.etListPointX.text.toString().toLong()
            wechatControlVO.listPointY = binding.etListPointY.text.toString().toLong()
            RedEnvelopePreferences.wechatControl = wechatControlVO
        }
    }

    override fun initView(view: View) {
        //无障碍开关状态
        binding.cbQqControl.setOnCheckedChangeListener { _, isChecked ->
            binding.cbQqControl.isChecked = !isChecked
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            Toast.makeText(view.context, getString(R.string.control_state_tips), Toast.LENGTH_SHORT)
                .show()
        }
        //监视通知开关状态
        binding.cbWechatNotificationControl.setOnCheckedChangeListener { _, isChecked ->
            wechatControlVO.isMonitorNotification = isChecked
            RedEnvelopePreferences.wechatControl = wechatControlVO
        }
        //监视列表开关状态
        binding.cbWechatChatControl.setOnCheckedChangeListener { _, isChecked ->
            wechatControlVO.isMonitorChat = isChecked
            RedEnvelopePreferences.wechatControl = wechatControlVO
        }
        //是否抢自己开关状态
        binding.cbIfGrabSelf.setOnCheckedChangeListener { _, isChecked ->
            wechatControlVO.ifGrabSelf = isChecked
            RedEnvelopePreferences.wechatControl = wechatControlVO
        }

        //设置红包按钮坐标
        binding.cbCustomClick.setOnCheckedChangeListener { _, isChecked ->
            wechatControlVO.isCustomClick = isChecked
            RedEnvelopePreferences.wechatControl = wechatControlVO
        }
        binding.etPointX.addTextChangedListener {
            if (binding.etPointX.text.isNullOrEmpty()) {
                wechatControlVO.pointX = 0
            } else {
                wechatControlVO.pointX = binding.etPointX.text.toString().toLong()
            }
            RedEnvelopePreferences.wechatControl = wechatControlVO
        }
        binding.etPointY.addTextChangedListener {
            if (binding.etPointY.text.isNullOrEmpty()) {
                wechatControlVO.pointY = 0
            } else {
                wechatControlVO.pointY = binding.etPointY.text.toString().toLong()
            }
            RedEnvelopePreferences.wechatControl = wechatControlVO
        }
        //设置红包弹窗点击坐标
        binding.btnSetRebBtnPoint.setOnClickListener {
            PhonePointActivity.start(
                requireActivity(),
                PhonePointActivity.SET_OPEN_RED_POINT_REQUEST_CODE
            )
        }

        //设置红包框坐标
        binding.cbCustomListClick.setOnCheckedChangeListener { _, isChecked ->
            wechatControlVO.isCustomListClick = isChecked
            RedEnvelopePreferences.wechatControl = wechatControlVO
        }
        binding.etListPointX.addTextChangedListener {
            if (binding.etListPointX.text.isNullOrEmpty()) {
                wechatControlVO.listPointX = 0
            } else {
                wechatControlVO.listPointX = binding.etListPointX.text.toString().toLong()
            }
            RedEnvelopePreferences.wechatControl = wechatControlVO
        }
        binding.etListPointY.addTextChangedListener {
            if (binding.etListPointY.text.isNullOrEmpty()) {
                wechatControlVO.listPointY = 0
            } else {
                wechatControlVO.listPointY = binding.etListPointY.text.toString().toLong()
            }
            RedEnvelopePreferences.wechatControl = wechatControlVO
        }
        //设置聊天窗点击坐标
        binding.btnSetRebBtnListPoint.setOnClickListener {
            PhonePointActivity.start(
                requireActivity(),
                PhonePointActivity.SET_OPEN_LIST_RED_POINT_REQUEST_CODE
            )
        }

        //设置延时
        binding.sbQqPutong.setOnSeekBarChangeListener(this)
        binding.sbQqLingqu.setOnSeekBarChangeListener(this)

        //设置群过滤
        binding.etTextFilters.setText(RedEnvelopePreferences.grabFilter)
        binding.etTextFilters.addTextChangedListener {
            RedEnvelopePreferences.grabFilter = binding.etTextFilters.text.toString()
        }

        //设置IP
        setupIpValidation(binding.etIp)

        binding.btnTest.setOnClickListener {
            GlobalScope.launch {
                val delayTime =
                    500L + (1000L * RedEnvelopePreferences.wechatControl.delayOpenTime / 10)
                LogUtils.d("start show delay open time:$delayTime")
                delay(delayTime)
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireActivity(), "延时打开 $delayTime ms", Toast.LENGTH_SHORT)
                        .show()
                    LogUtils.d("end show delay open time:$delayTime")
                }
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun initData() {
        wechatControlVO = RedEnvelopePreferences.wechatControl

        binding.cbWechatNotificationControl.isChecked = wechatControlVO.isMonitorNotification
        binding.cbWechatChatControl.isChecked = wechatControlVO.isMonitorChat
        binding.cbIfGrabSelf.isChecked = wechatControlVO.ifGrabSelf

        t_putong = wechatControlVO.delayOpenTime
        binding.tvQqPutong.text =
            getString(R.string.control_delay_open_red_envelope) + t_putong / 10.0 + "s"
        binding.sbQqPutong.progress = t_putong

        t_lingqu = wechatControlVO.delayCloseTime
        binding.sbQqLingqu.progress = t_lingqu - 1
        if (t_lingqu == 101) {
            binding.tvQqLingqu.text =
                getString(R.string.control_delay_close_red_envelope) + "不关闭"
        } else {
            binding.tvQqLingqu.text =
                getString(R.string.control_delay_close_red_envelope) + t_lingqu / 10.0 + "s"
        }

        binding.cbCustomClick.isChecked = wechatControlVO.isCustomClick
        binding.etPointX.setText(wechatControlVO.pointX.toString())
        binding.etPointY.setText(wechatControlVO.pointY.toString())

        binding.cbCustomListClick.isChecked = wechatControlVO.isCustomListClick
        binding.etListPointX.setText(wechatControlVO.listPointX.toString())
        binding.etListPointY.setText(wechatControlVO.listPointY.toString())

        binding.etIp.setText(WebSocketConst.IP)

        val mainActivity = activity as MainActivity
        updateControlView(mainActivity.checkStatus())
    }

    /**
     * 更新无障碍状态
     */
    fun updateControlView(checkStatus: Boolean) {
        val activity = requireActivity()
        if (activity is MainActivity) {
            if (checkStatus) binding.cbQqControl.setButtonDrawable(R.mipmap.switch_on)
            else binding.cbQqControl.setButtonDrawable(R.mipmap.switch_off)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        when (seekBar.id) {
            R.id.sb_qq_putong -> {
                LogUtils.d("sb_qq_pu_tong:$progress")
                t_putong = progress
                binding.tvQqPutong.text =
                    getString(R.string.control_delay_open_red_envelope) + t_putong / 10.0 + "s"
                wechatControlVO.delayOpenTime = t_putong
                RedEnvelopePreferences.wechatControl = wechatControlVO
            }

            R.id.sb_qq_lingqu -> {
                LogUtils.d("sb_qq_ling qu:$progress")
                t_lingqu = progress + 1
                binding.tvQqLingqu.text =
                    getString(R.string.control_delay_close_red_envelope) + t_lingqu / 10.0 + "s"
                if (t_lingqu == 101) {
                    binding.tvQqLingqu.text =
                        getString(R.string.control_delay_close_red_envelope) + "不关闭"
                }
                wechatControlVO.delayCloseTime = t_lingqu
                RedEnvelopePreferences.wechatControl = wechatControlVO
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {

    }


    //==============================================================================================
    // 校验IP
    //==============================================================================================


    /**
     *  validate IPv4 address
     */
    fun validateIPv4Address(ipAddress: String): Boolean {
        val ipPattern = "^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +
                "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
        return ipAddress.matches(Regex(ipPattern))
    }

    /**
     * 设置websocket连接的IP
     */
    private fun setupIpValidation(editTextIp: EditText) {
        editTextIp.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                charSequence: CharSequence?,
                start: Int,
                before: Int,
                after: Int
            ) {
                // No action needed before the text changes
            }

            override fun onTextChanged(
                charSequence: CharSequence?,
                start: Int,
                before: Int,
                after: Int
            ) {
                // When the text is being changed, validate the IP
                val ipAddress = charSequence.toString()
                if (validateIPv4Address(ipAddress)) {
                    // Valid IP address
                    editTextIp.setBackgroundColor(android.graphics.Color.GREEN) // Optional: Change background color on valid input
                } else {
                    // Invalid IP address
                    editTextIp.setBackgroundColor(android.graphics.Color.RED) // Optional: Change background color on invalid input
                }
            }

            override fun afterTextChanged(editable: Editable?) {
                // You can display Toast or any other feedback after text is changed
                val ipAddress = editable.toString()
                if (validateIPv4Address(ipAddress)) {
                    Toast.makeText(editTextIp.context, "Valid IPv4 Address", Toast.LENGTH_SHORT)
                        .show()
                    WebSocketConst.setNewIP(ipAddress)
                } else {
                    Toast.makeText(editTextIp.context, "Invalid IPv4 Address", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })
    }


    //==============================================================================================


}
