package com.carlos.grabredenvelope.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import com.carlos.grabredenvelope.R
import com.carlos.grabredenvelope.data.RedEnvelopePreferences
import com.carlos.grabredenvelope.databinding.FragmentEmojiBinding


class EmojiFragment : BaseFragment(R.layout.fragment_emoji) {

    private lateinit var binding: FragmentEmojiBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEmojiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        binding.etEmoji.setText(RedEnvelopePreferences.autoText)
        binding.etEmoji.doAfterTextChanged {
            RedEnvelopePreferences.autoText = binding.etEmoji.text.toString()
        }

        binding.npTimes.minValue = 0
        binding.npTimes.maxValue = 100
        binding.npTimes.value = RedEnvelopePreferences.emojiTimes
        binding.npTimes.setOnValueChangedListener { picker, oldVal, newVal ->
            RedEnvelopePreferences.emojiTimes = newVal
        }

        binding.npInterval.minValue = 0
        binding.npInterval.maxValue = 3000
        binding.npInterval.value = RedEnvelopePreferences.emojiInterval
        binding.npInterval.setOnValueChangedListener { picker, oldVal, newVal ->
            RedEnvelopePreferences.emojiInterval = newVal
        }


        binding.cbEmojiControl.setOnCheckedChangeListener { buttonView, isChecked ->
            binding.cbEmojiControl.isChecked = isChecked
            RedEnvelopePreferences.emojiState = binding.cbEmojiControl.isChecked
            updateControlView()
        }
        updateControlView()
    }

    private fun updateControlView() {
        if (RedEnvelopePreferences.emojiState) binding.cbEmojiControl?.setButtonDrawable(R.mipmap.switch_on)
        else binding.cbEmojiControl?.setButtonDrawable(R.mipmap.switch_off)
    }

}