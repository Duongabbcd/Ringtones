package com.example.ringtone.screen.ringtone.player.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.example.ringtone.databinding.DialogCreditBinding
import com.example.ringtone.remote.model.Ringtone
import com.example.ringtone.R
import com.example.ringtone.screen.intro.IntroFragmentNew

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