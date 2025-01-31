package com.carlos.grabredenvelope.fragment

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import com.carlos.grabredenvelope.R
import com.carlos.grabredenvelope.data.RedEnvelopePreferences
import kotlinx.android.synthetic.main.fragment_emoji.cb_emoji_control
import kotlinx.android.synthetic.main.fragment_emoji.et_emoji
import kotlinx.android.synthetic.main.fragment_emoji.np_interval
import kotlinx.android.synthetic.main.fragment_emoji.np_times

class EmojiFragment : BaseFragment(R.layout.fragment_emoji) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        et_emoji.setText(RedEnvelopePreferences.autoText)
        et_emoji.doAfterTextChanged {
            RedEnvelopePreferences.autoText = et_emoji.text.toString()
        }

        np_times.minValue = 0
        np_times.maxValue = 100
        np_times.value = RedEnvelopePreferences.emojiTimes
        np_times.setOnValueChangedListener { picker, oldVal, newVal ->
            RedEnvelopePreferences.emojiTimes = newVal
        }

        np_interval.minValue = 0
        np_interval.maxValue = 3000
        np_interval.value = RedEnvelopePreferences.emojiInterval
        np_interval.setOnValueChangedListener { picker, oldVal, newVal ->
            RedEnvelopePreferences.emojiInterval = newVal
        }


        cb_emoji_control.setOnCheckedChangeListener { buttonView, isChecked ->
            cb_emoji_control.isChecked = isChecked
            RedEnvelopePreferences.emojiState = cb_emoji_control.isChecked
            updateControlView()
        }
        updateControlView()
    }

    private fun updateControlView() {
        if (RedEnvelopePreferences.emojiState) cb_emoji_control?.setButtonDrawable(R.mipmap.switch_on)
        else cb_emoji_control?.setButtonDrawable(R.mipmap.switch_off)
    }

}