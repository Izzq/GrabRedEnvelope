package com.carlos.grabredenvelope.extensions

import android.view.HapticFeedbackConstants
import android.view.View


fun View.beVisibleIf(beVisible: Boolean) = if (beVisible) beVisible() else beGone()


fun View.beVisible() {
    visibility = View.VISIBLE
}

fun View.beGone() {
    visibility = View.GONE
}

fun View.isVisible() = visibility == View.VISIBLE

fun View.isInvisible() = visibility == View.INVISIBLE

fun View.isGone() = visibility == View.GONE

fun View.performHapticFeedback() = performHapticFeedback(
    HapticFeedbackConstants.VIRTUAL_KEY,
    HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
)

