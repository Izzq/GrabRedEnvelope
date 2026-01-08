package com.carlos.grabredenvelope.fragment

import android.app.Activity
import android.content.Intent
import android.os.Build
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
 *                             _ooOoo_
 *                            o8888888o
 *                            88" . "88
 *                            (| -_- |)
 *                            O\  =  /O
 *                         ____/`---'\____
 *                       .'  \\|     |//  `.
 *                      /  \\|||  :  |||//  \
 *                     /  _||||| -:- |||||-  \
 *                     |   | \\\  -  /// |   |
 *                     | \_|  ''\---/''  |   |
 *                     \  .-\__  `-`  ___/-. /
 *                   ___`. .'  /--.--\  `. . __
 *                ."" '<  `.___\_<|>_/___.'  >'"".
 *               | | :  `- \`.;`\ _ /`;.`/ - ` : | |
 *               \  \ `-.   \_ __\ /__ _/   .-` /  /
 *          ======`-.____`-.___\_____/___.-`____.-'======
 *                             `=---='
 *          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
 *                     佛祖保佑        永无BUG
 *            佛曰:
 *                   写字楼里写字间，写字间里程序员；
 *                   程序人员写程序，又拿程序换酒钱。
 *                   酒醒只在网上坐，酒醉还来网下眠；
 *                   酒醉酒醒日复日，网上网下年复年。
 *                   但愿老死电脑间，不愿鞠躬老板前；
 *                   奔驰宝马贵者趣，公交自行程序员。
 *                   别人笑我忒疯癫，我笑自己命太贱；
 *                   不见满街漂亮妹，哪个归得程序员？
 */

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

        init(view)

        loadSaveData()
    }

    private fun init(view: View) {
        binding.cbQqControl.setOnCheckedChangeListener { buttonView, isChecked ->
            binding.cbQqControl.isChecked = !isChecked
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            Toast.makeText(view.context, "辅助功能找到（抢微信红包）开启或关闭。", Toast.LENGTH_SHORT)
                .show()
        }

        binding.sbQqPutong.setOnSeekBarChangeListener(this)
        binding.sbQqLingqu.setOnSeekBarChangeListener(this)

        binding.cbWechatNotificationControl.setOnCheckedChangeListener { buttonView, isChecked ->
            wechatControlVO.isMonitorNotification = isChecked
            RedEnvelopePreferences.wechatControl = wechatControlVO
        }
        binding.cbWechatChatControl.setOnCheckedChangeListener { buttonView, isChecked ->
            wechatControlVO.isMonitorChat = isChecked
            RedEnvelopePreferences.wechatControl = wechatControlVO
        }
        binding.cbIfGrabSelf.setOnCheckedChangeListener { buttonView, isChecked ->
            wechatControlVO.ifGrabSelf = isChecked
            RedEnvelopePreferences.wechatControl = wechatControlVO
        }


        binding.cbCustomClick.setOnCheckedChangeListener { buttonView, isChecked ->
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

        binding.cbCustomListClick.setOnCheckedChangeListener { buttonView, isChecked ->
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.llCustomClick.visibility = View.VISIBLE
            binding.llCustomListClick.visibility = View.VISIBLE
        } else {
            binding.llCustomClick.visibility = View.GONE
            binding.llCustomListClick.visibility = View.GONE
        }

        binding.etTextFilters.setText(RedEnvelopePreferences.grabFilter)
        binding.etTextFilters.addTextChangedListener {
            RedEnvelopePreferences.grabFilter = binding.etTextFilters.text.toString()
        }

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

//
        //设置红包弹窗点击坐标
        binding.btnSetRebBtnPoint.setOnClickListener {
            PhonePointActivity.start(
                requireActivity(),
                PhonePointActivity.SET_OPEN_RED_POINT_REQUEST_CODE
            )
        }
        //设置聊天窗点击坐标
        binding.btnSetRebBtnListPoint.setOnClickListener {
            PhonePointActivity.start(
                requireActivity(),
                PhonePointActivity.SET_OPEN_LIST_RED_POINT_REQUEST_CODE
            )
        }


        //设置IP
        setupIpValidation(binding.etIp)

    }


    private fun loadSaveData() {
        binding.cbWechatNotificationControl.isChecked =
            RedEnvelopePreferences.wechatControl.isMonitorNotification
        binding.cbWechatChatControl.isChecked = RedEnvelopePreferences.wechatControl.isMonitorChat
        binding.cbIfGrabSelf.isChecked = RedEnvelopePreferences.wechatControl.ifGrabSelf
        LogUtils.d("wechatControl:" + RedEnvelopePreferences.wechatControl.toString())

        wechatControlVO = RedEnvelopePreferences.wechatControl
        t_putong = wechatControlVO.delayOpenTime
        binding.tvQqPutong.text = "领取红包延迟时间：" + t_putong / 10.0 + "s"
        binding.sbQqPutong.progress = t_putong

        t_lingqu = wechatControlVO.delayCloseTime
        binding.sbQqLingqu.progress = t_lingqu - 1
        if (t_lingqu == 101) {
            binding.tvQqLingqu.text = "红包领取页关闭时间：" + "不关闭"
        } else {
            binding.tvQqLingqu.text = "红包领取页关闭时间：" + t_lingqu / 10.0 + "s"
        }


        binding.cbCustomClick.isChecked = RedEnvelopePreferences.wechatControl.isCustomClick
        binding.etPointX.setText(RedEnvelopePreferences.wechatControl.pointX.toString())
        binding.etPointY.setText(RedEnvelopePreferences.wechatControl.pointY.toString())

        binding.cbCustomListClick.isChecked = RedEnvelopePreferences.wechatControl.isCustomListClick
        binding.etListPointX.setText(RedEnvelopePreferences.wechatControl.listPointX.toString())
        binding.etListPointY.setText(RedEnvelopePreferences.wechatControl.listPointY.toString())


        binding.etIp.setText(WebSocketConst.IP)

        val mainActivity = activity as MainActivity
        updateControlView(mainActivity.checkStatus())
    }

    override fun onResume() {
        super.onResume()
        val mainActivity = activity as MainActivity
        updateControlView(mainActivity.checkStatus())
    }

    fun updateControlView(checkStatus: Boolean) {
        val activity = requireActivity()
        if (activity is MainActivity) {
            if (checkStatus) binding.cbQqControl?.setButtonDrawable(R.mipmap.switch_on)
            else binding.cbQqControl?.setButtonDrawable(R.mipmap.switch_off)
        }


    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        when (seekBar.id) {
            R.id.sb_qq_putong -> {
                LogUtils.d("sb_qq_putong:$progress")
                t_putong = progress
                binding.tvQqPutong.text = "领取红包延迟时间：" + t_putong / 10.0 + "s"
                wechatControlVO.delayOpenTime = t_putong
                RedEnvelopePreferences.wechatControl = wechatControlVO
            }

            R.id.sb_qq_lingqu -> {
                LogUtils.d("sb_qq_lingqu:$progress")
                t_lingqu = progress + 1
                binding.tvQqLingqu.text = "红包领取页关闭延迟时间：" + t_lingqu / 10.0 + "s"
                if (t_lingqu == 101) {
                    binding.tvQqLingqu.text = "红包领取页关闭时间：" + "不关闭"
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
