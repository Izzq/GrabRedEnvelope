package com.carlos.cutils.util

import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.carlos.cutils.CUtils

/**
 * Github: https://github.com/xbdcc/.
 * Created by Carlos on 2019/2/20.
 */
object AccessibilityServiceUtils {

    fun findAndClickFirstNodeInfoByText(
        text: String,
        isReverse: Boolean = false,
        accessibilityNodeInfo: AccessibilityNodeInfo?
    ) {
        val node = accessibilityNodeInfo?.findAccessibilityNodeInfosByText(text)
        clickFirstNodeInfo(node, isReverse)
    }

    fun findAndClickFirstNodeInfoByViewId(
        viewId: String,
        isReverse: Boolean = false,
        accessibilityNodeInfo: AccessibilityNodeInfo?
    ) {
        val accessibilityNodeInfos =
            accessibilityNodeInfo?.findAccessibilityNodeInfosByViewId(viewId)
        clickFirstNodeInfo(accessibilityNodeInfos, isReverse)
    }

    fun clickFirstNodeInfo(
        accessibilityNodeInfos: MutableList<AccessibilityNodeInfo>?,
        isReverse: Boolean = false
    ) {
        if (accessibilityNodeInfos.isNullOrEmpty()) {
            return
        }
        if (isReverse) {
            accessibilityNodeInfos.last().performAction(AccessibilityNodeInfo.ACTION_CLICK)
        } else {
            accessibilityNodeInfos.first().performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
    }

    fun isExistNodeInfosByText(text: String, accessibilityNodeInfo: AccessibilityNodeInfo?) =
        accessibilityNodeInfo?.findAccessibilityNodeInfosByText(text)?.isNotEmpty() ?: false

    fun isExistNodeInfosByViewId(viewId: String, accessibilityNodeInfo: AccessibilityNodeInfo?) =
        accessibilityNodeInfo?.findAccessibilityNodeInfosByViewId(viewId)?.isNotEmpty() ?: false

    fun getNodeInfosByText(text: String, accessibilityNodeInfo: AccessibilityNodeInfo?) =
        accessibilityNodeInfo?.findAccessibilityNodeInfosByText(text)

    fun getNodeInfosByViewId(viewId: String, accessibilityNodeInfo: AccessibilityNodeInfo?) =
        accessibilityNodeInfo?.findAccessibilityNodeInfosByViewId(viewId)

    fun findAndClickFirstNodeInfoByViewId(
        viewId: String,
        childExistId: String,
        childNotExistIds: String,
        isJustClickLeft: Boolean = false,
        isReverse: Boolean = false,
        accessibilityNodeInfo: AccessibilityNodeInfo?,
        callback: ((Boolean) -> Unit)? = null,
    ): Boolean {
        var accessibilityNodeInfos =
            accessibilityNodeInfo?.findAccessibilityNodeInfosByViewId(viewId) ?: return false
        if (isReverse) accessibilityNodeInfos = accessibilityNodeInfos.reversed()
        for (accessibilityNodeInfo in accessibilityNodeInfos) {
            if (isExistNodeInfosByViewId(childNotExistIds, accessibilityNodeInfo))
                continue
            if (!isExistNodeInfosByViewId(childExistId, accessibilityNodeInfo))
                continue
            if (isJustClickLeft && !isLeft(accessibilityNodeInfo))
                continue

            val result: Boolean =
                accessibilityNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)

            if (accessibilityNodeInfo.isClickable) {
                // 获取控件的边界
                val bounds = Rect()
                accessibilityNodeInfo.getBoundsInScreen(bounds)

                // 获取控件的左上角坐标 (x, y) 和右下角坐标 (x2, y2)
                val x1 = bounds.left // 控件左上角 X 坐标
                val y1 = bounds.top // 控件左上角 Y 坐标
                val x2 = bounds.right // 控件右下角 X 坐标
                val y2 = bounds.bottom // 控件右下角 Y 坐标

                Log.d("RedEnvelopeLog", "內容 Control bounds: ($x1, $y1) to ($x2, $y2)")
            } else {
                val node = accessibilityNodeInfo.parent ?: accessibilityNodeInfo

                // 获取控件的边界
                val bounds = Rect()
                node.getBoundsInScreen(bounds)

                // 获取控件的左上角坐标 (x, y) 和右下角坐标 (x2, y2)
                val x1 = bounds.left // 控件左上角 X 坐标
                val y1 = bounds.top // 控件左上角 Y 坐标
                val x2 = bounds.right // 控件右下角 X 坐标
                val y2 = bounds.bottom // 控件右下角 Y 坐标

                Log.d("RedEnvelopeLog", "parent 內容 Control bounds: ($x1, $y1) to ($x2, $y2)")
            }

//            if (result) {
//                // 获取控件的边界
//                val bounds = Rect()
//                accessibilityNodeInfo.getBoundsInScreen(bounds)
//
//                // 获取控件的左上角坐标 (x, y) 和右下角坐标 (x2, y2)
//                val x1 = bounds.left // 控件左上角 X 坐标
//                val y1 = bounds.top // 控件左上角 Y 坐标
//                val x2 = bounds.right // 控件右下角 X 坐标
//                val y2 = bounds.bottom // 控件右下角 Y 坐标
//
//                Log.d("RedEnvelopeLog", "內容 Control bounds: ($x1, $y1) to ($x2, $y2)")
//            }
            callback?.invoke(true)
            return true

        }
        callback?.invoke(false)
        return false
    }

    private fun isLeft(accessibilityNodeInfo: AccessibilityNodeInfo): Boolean {
        val rect = Rect()
        accessibilityNodeInfo.getBoundsInScreen(rect)
        val width = CUtils.cContext.resources.displayMetrics.widthPixels
        return rect.centerX() < width / 2
    }

    fun findAndClickFirstNodeInfoByViewIdContainsText(
        viewId: String,
        childId: String,
        childIdContainsText: String,
        isReverse: Boolean = false,
        accessibilityNodeInfo: AccessibilityNodeInfo?,
        callback: ((Boolean) -> Unit)? = null,
    ): Boolean {
        var accessibilityNodeInfos =
            accessibilityNodeInfo?.findAccessibilityNodeInfosByViewId(viewId) ?: return false
        if (isReverse) accessibilityNodeInfos = accessibilityNodeInfos.reversed()
        for (accessibilityNodeInfo in accessibilityNodeInfos) {
            val childNodeInfo = getNodeInfosByViewId(childId, accessibilityNodeInfo)
            if (childNodeInfo.isNullOrEmpty()) {
                callback?.invoke(false)
                return false
            }
            if (childNodeInfo.first().text.contains(childIdContainsText)) {
                accessibilityNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)

//                // 获取控件的边界
//                val bounds = Rect()
//                accessibilityNodeInfo.getBoundsInScreen(bounds)
//
//                // 获取控件的左上角坐标 (x, y) 和右下角坐标 (x2, y2)
//                val x1 = bounds.left // 控件左上角 X 坐标
//                val y1 = bounds.top // 控件左上角 Y 坐标
//                val x2 = bounds.right // 控件右下角 X 坐标
//                val y2 = bounds.bottom // 控件右下角 Y 坐标
//
//                Log.d(
//                    "RedEnvelopeLog",
//                    "列表 ${childIdContainsText} Control bounds: ($x1, $y1) to ($x2, $y2)"
//                )
                callback?.invoke(true)
                return true
            }
        }
        callback?.invoke(false)
        return false
    }

}