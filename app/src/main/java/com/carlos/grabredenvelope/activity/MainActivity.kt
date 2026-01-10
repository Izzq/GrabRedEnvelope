package com.carlos.grabredenvelope.activity

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.PermissionUtils.SimpleCallback
import com.carlos.cutils.base.adapter.CBaseMyPagerAdapter
import com.carlos.grabredenvelope.R
import com.carlos.grabredenvelope.databinding.ActivityMainBinding
import com.carlos.grabredenvelope.extensions.viewBinding
import com.carlos.grabredenvelope.fragment.AboutFragment
import com.carlos.grabredenvelope.fragment.ControlFragment
import com.carlos.grabredenvelope.fragment.EmojiFragment
import com.carlos.grabredenvelope.fragment.IMainFragment
import com.carlos.grabredenvelope.fragment.RecordFragment
import com.carlos.grabredenvelope.notification.NotificationKits
import com.carlos.grabredenvelope.services.wechat.WechatService


/**
 * Github: https://github.com/xbdcc/.
 * Created by 小不点 on 2016/2/14.
 */
open class MainActivity : BaseActivity() {

    private val binding by viewBinding(ActivityMainBinding::inflate)

    private var titles = mutableListOf<String>()

    var fragments = mutableListOf<Fragment>(
        ControlFragment(),
        AboutFragment(),
        RecordFragment(),
        EmojiFragment()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initView()
        requestPermissionNotification()
        addListener()
    }

    private fun initView() {
        titles.add(getString(R.string.main_tab_control))
        titles.add(getString(R.string.main_tab_tutorials))
        titles.add(getString(R.string.main_tab_history))
        titles.add(getString(R.string.main_tab_expression))

        val adapter = CBaseMyPagerAdapter(supportFragmentManager, fragments, titles)
        binding.viewPager.adapter = adapter
        binding.slidingTabs.setupWithViewPager(binding.viewPager)
        binding.viewPager.offscreenPageLimit = fragments.size
    }

    private fun addListener() {
        addAccessibilityServiceListener(object :
            AccessibilityServiceListeners {
            override fun updateStatus(boolean: Boolean) {
                LogUtils.d("updateStatus:$boolean")
                val fragment = fragments.getOrNull(0)
                if (fragment is ControlFragment) {
                    fragment.updateControlView(boolean)
                }
            }
        }, "${packageName}/${WechatService::class.java.name}")
    }


    // 接收第二个 Activity 返回的结果
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        fragments.forEach {
            if (it is IMainFragment) {
                it.onActivityResult(requestCode, resultCode, data)
            }
        }
    }


    private fun requestPermissionNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            PermissionUtils.permission(Manifest.permission.POST_NOTIFICATIONS)
                .callback(object : SimpleCallback {
                    override fun onGranted() {
                        //启动前台服务-开启常驻通知栏
                        NotificationKits.startPermanentNotification(this@MainActivity)
                    }

                    override fun onDenied() {

                    }
                }).request()
        }
    }

}