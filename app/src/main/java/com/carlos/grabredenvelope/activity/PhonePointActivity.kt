package com.carlos.grabredenvelope.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.LogUtils
import com.carlos.grabredenvelope.R
import com.carlos.grabredenvelope.view.OverlayView
import com.gyf.immersionbar.BarHide
import com.gyf.immersionbar.ktx.immersionBar
import kotlinx.android.synthetic.main.activity_phone_point.iv_back

/**
 * 获取点击坐标
 */
class PhonePointActivity : AppCompatActivity() {

    companion object {

        //设置红包弹窗点击坐标
        const val SET_OPEN_RED_POINT_REQUEST_CODE = 1000

        //设置聊天窗点击坐标
        const val SET_OPEN_LIST_RED_POINT_REQUEST_CODE = 2000

        fun start(context: Activity, requestCode: Int) {
            val intent = Intent(context, PhonePointActivity::class.java)
            context.startActivityForResult(intent, requestCode)
        }

    }

    private val REQ_PICK_IMAGE = 1001
    private val REQUEST_STORAGE_PERMISSION = 2001

    private lateinit var imageView: ImageView
    private lateinit var overlayView: OverlayView

    private var mViewX = 0
    private var mViewY = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_point)

        hideSystemUI()

        imageView = findViewById(R.id.imageView)
        overlayView = findViewById(R.id.overlayView)


        // 请求权限
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            checkStoragePermission()
//        } else {
//            // 如果是 Android 6.0 以下版本，直接开始
//            pickImage()
//        }

        // 如果是 Android 6.0 以下版本，直接开始
        pickImage()

        imageView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                handleClick(event)
                true
            } else {
                false
            }
        }

        iv_back.setOnClickListener {
            val intent = Intent()
            intent.putExtra("x", mViewX)
            intent.putExtra("y", mViewY)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    // 隐藏状态栏和导航栏
    private fun hideSystemUI() {
        immersionBar {
            hideBar(BarHide.FLAG_HIDE_BAR)
        }
    }

    // 当用户重新聚焦到应用时，再次隐藏系统UI
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }

    // 检查存储权限
    private fun checkStoragePermission() {
        val permission = Manifest.permission.READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(
                this,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // 如果没有权限，请求权限
            ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_STORAGE_PERMISSION)
        } else {
            // 如果已有权限，继续执行
            pickImage()
        }
    }

    // 权限请求结果处理
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 用户同意权限，开始选择图片
                    pickImage()
                } else {
                    // 权限被拒绝，提示用户
                    // 可以通过 Toast 或其他方式告知用户权限未授权
                    LogUtils.w("权限被拒绝，无法访问图库")
                }
            }
        }
    }

    // 打开图库选择图片
    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQ_PICK_IMAGE)
    }

    // 处理图片选择返回结果
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_PICK_IMAGE && resultCode == RESULT_OK) {
            val uri: Uri? = data?.data
            imageView.setImageURI(uri)
        }
    }

    // 处理点击事件并显示坐标
    private fun handleClick(event: MotionEvent) {
        val drawable = imageView.drawable
        if (drawable == null) return

        val rect = getImageDisplayRect(imageView)

        val viewX = event.x
        val viewY = event.y

        // 不在图片区域内
        if (!rect.contains(viewX, viewY)) return

        val imgW = drawable.intrinsicWidth
        val imgH = drawable.intrinsicHeight

        val imageX = (viewX - rect.left) * imgW / rect.width()
        val imageY = (viewY - rect.top) * imgH / rect.height()

        // 画点
        val showText = "(${imageX.toInt()}, ${imageY.toInt()})"
        overlayView.setPoint(viewX, viewY, showText)

        mViewX = viewX.toInt()
        mViewY = viewY.toInt()

        // 打印坐标
        LogUtils.d("ImageView坐标: x=${viewX.toInt()}, y=${viewY.toInt()}")
        LogUtils.d("图片坐标: x=${imageX.toInt()}, y=${imageY.toInt()}")
    }

    /**
     * 获取图片在 ImageView 中显示的真实区域
     */
    private fun getImageDisplayRect(imageView: ImageView): RectF {
        val rect = RectF()
        val drawable = imageView.drawable
        drawable?.let {
            rect.set(0f, 0f, it.intrinsicWidth.toFloat(), it.intrinsicHeight.toFloat())
            imageView.imageMatrix.mapRect(rect)
        }
        return rect
    }


}
