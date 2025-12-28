package com.carlos.grabredenvelope.fragment

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.blankj.utilcode.util.LogUtils
import com.carlos.grabredenvelope.R
import com.carlos.grabredenvelope.activity.MainActivity
import com.carlos.grabredenvelope.activity.PhonePointActivity
import com.carlos.grabredenvelope.dao.WechatControlVO
import com.carlos.grabredenvelope.data.RedEnvelopePreferences
import kotlinx.android.synthetic.main.fragment_control.btnTest
import kotlinx.android.synthetic.main.fragment_control.btn_set_reb_btn_list_point
import kotlinx.android.synthetic.main.fragment_control.btn_set_reb_btn_point
import kotlinx.android.synthetic.main.fragment_control.cb_custom_click
import kotlinx.android.synthetic.main.fragment_control.cb_custom_list_click
import kotlinx.android.synthetic.main.fragment_control.cb_if_grab_self
import kotlinx.android.synthetic.main.fragment_control.cb_qq_control
import kotlinx.android.synthetic.main.fragment_control.cb_wechat_chat_control
import kotlinx.android.synthetic.main.fragment_control.cb_wechat_notification_control
import kotlinx.android.synthetic.main.fragment_control.et_list_pointX
import kotlinx.android.synthetic.main.fragment_control.et_list_pointY
import kotlinx.android.synthetic.main.fragment_control.et_pointX
import kotlinx.android.synthetic.main.fragment_control.et_pointY
import kotlinx.android.synthetic.main.fragment_control.et_text_filters
import kotlinx.android.synthetic.main.fragment_control.ll_custom_click
import kotlinx.android.synthetic.main.fragment_control.ll_custom_list_click
import kotlinx.android.synthetic.main.fragment_control.sb_qq_lingqu
import kotlinx.android.synthetic.main.fragment_control.sb_qq_putong
import kotlinx.android.synthetic.main.fragment_control.tv_qq_lingqu
import kotlinx.android.synthetic.main.fragment_control.tv_qq_putong
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)

        loadSaveData()
    }

    private fun init(view: View) {
        cb_qq_control.setOnCheckedChangeListener { buttonView, isChecked ->
            cb_qq_control.isChecked = !isChecked
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            Toast.makeText(view.context, "辅助功能找到（抢微信红包）开启或关闭。", Toast.LENGTH_SHORT)
                .show()
        }

        sb_qq_putong.setOnSeekBarChangeListener(this)
        sb_qq_lingqu.setOnSeekBarChangeListener(this)


        cb_wechat_notification_control.setOnCheckedChangeListener { buttonView, isChecked ->
            wechatControlVO.isMonitorNotification = isChecked
            RedEnvelopePreferences.wechatControl = wechatControlVO
        }
        cb_wechat_chat_control.setOnCheckedChangeListener { buttonView, isChecked ->
            wechatControlVO.isMonitorChat = isChecked
            RedEnvelopePreferences.wechatControl = wechatControlVO
        }
        cb_if_grab_self.setOnCheckedChangeListener { buttonView, isChecked ->
            wechatControlVO.ifGrabSelf = isChecked
            RedEnvelopePreferences.wechatControl = wechatControlVO
        }


        cb_custom_click.setOnCheckedChangeListener { buttonView, isChecked ->
            wechatControlVO.isCustomClick = isChecked
            RedEnvelopePreferences.wechatControl = wechatControlVO
        }
        et_pointX.addTextChangedListener {
            if (et_pointX.text.isNullOrEmpty()) {
                wechatControlVO.pointX = 0
            } else {
                wechatControlVO.pointX = et_pointX.text.toString().toLong()
            }
            RedEnvelopePreferences.wechatControl = wechatControlVO
        }
        et_pointY.addTextChangedListener {
            if (et_pointY.text.isNullOrEmpty()) {
                wechatControlVO.pointY = 0
            } else {
                wechatControlVO.pointY = et_pointY.text.toString().toLong()
            }
            RedEnvelopePreferences.wechatControl = wechatControlVO
        }

        cb_custom_list_click.setOnCheckedChangeListener { buttonView, isChecked ->
            wechatControlVO.isCustomListClick = isChecked
            RedEnvelopePreferences.wechatControl = wechatControlVO
        }
        et_list_pointX.addTextChangedListener {
            if (et_list_pointX.text.isNullOrEmpty()) {
                wechatControlVO.listPointX = 0
            } else {
                wechatControlVO.listPointX = et_list_pointX.text.toString().toLong()
            }
            RedEnvelopePreferences.wechatControl = wechatControlVO
        }
        et_list_pointY.addTextChangedListener {
            if (et_list_pointY.text.isNullOrEmpty()) {
                wechatControlVO.listPointY = 0
            } else {
                wechatControlVO.listPointY = et_list_pointY.text.toString().toLong()
            }
            RedEnvelopePreferences.wechatControl = wechatControlVO
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ll_custom_click.visibility = View.VISIBLE
            ll_custom_list_click.visibility = View.VISIBLE
        } else {
            ll_custom_click.visibility = View.GONE
            ll_custom_list_click.visibility = View.GONE
        }

        et_text_filters.setText(RedEnvelopePreferences.grabFilter)
        et_text_filters.addTextChangedListener {
            RedEnvelopePreferences.grabFilter = et_text_filters.text.toString()
        }

        btnTest.setOnClickListener {
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
        btn_set_reb_btn_point.setOnClickListener {
            PhonePointActivity.start(
                requireActivity(),
                PhonePointActivity.SET_OPEN_RED_POINT_REQUEST_CODE
            )
        }
        //设置聊天窗点击坐标
        btn_set_reb_btn_list_point.setOnClickListener {
            PhonePointActivity.start(
                requireActivity(),
                PhonePointActivity.SET_OPEN_LIST_RED_POINT_REQUEST_CODE
            )
        }
    }


    private fun loadSaveData() {
        cb_wechat_notification_control.isChecked =
            RedEnvelopePreferences.wechatControl.isMonitorNotification
        cb_wechat_chat_control.isChecked = RedEnvelopePreferences.wechatControl.isMonitorChat
        cb_if_grab_self.isChecked = RedEnvelopePreferences.wechatControl.ifGrabSelf
        LogUtils.d("wechatControl:" + RedEnvelopePreferences.wechatControl.toString())

        wechatControlVO = RedEnvelopePreferences.wechatControl
        t_putong = wechatControlVO.delayOpenTime
        tv_qq_putong.text = "领取红包延迟时间：" + t_putong / 10.0 + "s"
        sb_qq_putong.progress = t_putong

        t_lingqu = wechatControlVO.delayCloseTime
        sb_qq_lingqu.progress = t_lingqu - 1
        if (t_lingqu == 101) {
            tv_qq_lingqu.text = "红包领取页关闭时间：" + "不关闭"
        } else {
            tv_qq_lingqu.text = "红包领取页关闭时间：" + t_lingqu / 10.0 + "s"
        }


        cb_custom_click.isChecked = RedEnvelopePreferences.wechatControl.isCustomClick
        et_pointX.setText(RedEnvelopePreferences.wechatControl.pointX.toString())
        et_pointY.setText(RedEnvelopePreferences.wechatControl.pointY.toString())

        cb_custom_list_click.isChecked = RedEnvelopePreferences.wechatControl.isCustomListClick
        et_list_pointX.setText(RedEnvelopePreferences.wechatControl.listPointX.toString())
        et_list_pointY.setText(RedEnvelopePreferences.wechatControl.listPointY.toString())

        val mainActivity = activity as MainActivity
        updateControlView(mainActivity.checkStatus())
    }

    fun updateControlView(boolean: Boolean) {
        if (boolean) cb_qq_control?.setButtonDrawable(R.mipmap.switch_on)
        else cb_qq_control?.setButtonDrawable(R.mipmap.switch_off)
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        when (seekBar.id) {
            R.id.sb_qq_putong -> {
                LogUtils.d("sb_qq_putong:$progress")
                t_putong = progress
                tv_qq_putong.text = "领取红包延迟时间：" + t_putong / 10.0 + "s"
                wechatControlVO.delayOpenTime = t_putong
                RedEnvelopePreferences.wechatControl = wechatControlVO
            }

            R.id.sb_qq_lingqu -> {
                LogUtils.d("sb_qq_lingqu:$progress")
                t_lingqu = progress + 1
                tv_qq_lingqu.text = "红包领取页关闭延迟时间：" + t_lingqu / 10.0 + "s"
                if (t_lingqu == 101) {
                    tv_qq_lingqu.text = "红包领取页关闭时间：" + "不关闭"
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
            et_pointX.setText("$x")
            et_pointY.setText("$y")

            wechatControlVO.pointX = et_pointX.text.toString().toLong()
            wechatControlVO.pointY = et_pointY.text.toString().toLong()
            RedEnvelopePreferences.wechatControl = wechatControlVO
        } else if (requestCode == PhonePointActivity.SET_OPEN_LIST_RED_POINT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // 获取返回的坐标
            val x = data?.getIntExtra("x", 0) ?: 0
            val y = data?.getIntExtra("y", 0) ?: 0

            // 显示坐标
            et_list_pointX.setText("$x")
            et_list_pointY.setText("$y")

            wechatControlVO.listPointX = et_list_pointX.text.toString().toLong()
            wechatControlVO.listPointY = et_list_pointY.text.toString().toLong()
            RedEnvelopePreferences.wechatControl = wechatControlVO
        }
    }
}
