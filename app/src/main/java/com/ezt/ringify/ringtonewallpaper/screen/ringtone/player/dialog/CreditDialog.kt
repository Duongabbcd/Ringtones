package com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.ezt.ringify.ringtonewallpaper.databinding.DialogCreditBinding
import com.ezt.ringify.ringtonewallpaper.remote.model.Ringtone
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.screen.intro.IntroFragmentNew

class CreditDialog(
    context: Context
) : Dialog(context) {
    private val binding by lazy { DialogCreditBinding.inflate(layoutInflater) }

    init {
        setContentView(binding.root)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    fun setCreditContent(ringtone: Ringtone) {
        val full =  context.getString(
            R.string.credits_desc,
            ringtone.author.name,
            ringtone.name
        )

        val target = ringtone.author.name
        val target2 =  ringtone.name
        IntroFragmentNew.setSpannableString(full, listOf(target, target2), binding.creditDesc)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            okBtn.setOnClickListener {
                dismiss()
            }
        }
    }
}