package com.ezt.ringify.ringtonewallpaper.screen.ringtone.player.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.TextView
import com.ezt.ringify.ringtonewallpaper.R
import com.ezt.ringify.ringtonewallpaper.databinding.DialogCreditBinding
import com.ezt.ringify.ringtonewallpaper.remote.model.Ringtone
import com.ezt.ringify.ringtonewallpaper.screen.intro.IntroFragmentNew

class CreditDialog(
    context: Context
) : Dialog(context) {
    private val binding by lazy { DialogCreditBinding.inflate(layoutInflater) }

    init {
        setContentView(binding.root)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    fun setCreditRingtone(ringtone: Ringtone) {
        val creditWeb = "https://creativecommons.org/licenses/by/3.0/"
        val shorterCreditWeb = "https://creativecommons.org"

        // Construct the full string using the shorter label
        val full = context.getString(
            R.string.credits_desc,
            ringtone.author?.name ?: context.resources.getString(R.string.unknwon_author),
            shorterCreditWeb
        )

        // Highlighted parts
        val authorName = ringtone.author?.name ?: context.resources.getString(R.string.unknwon_author)
        val ringtoneName = ringtone.name
        val creditLabel = shorterCreditWeb

        // Call the updated setSpannableString
        setSpannableString(
            fullText = full,
            highlightTargets = listOf(authorName, ringtoneName),
            clickableTargets = mapOf(creditLabel to creditWeb),
            textView = binding.creditDesc
        )
    }


    fun setCreditWallpaper() {
        val creditWeb = "EZT"
        val authorName = "EZTech"
        val full = context.getString(
            R.string.credits_desc,
            authorName,
            creditWeb
        )

        val target = authorName
        val target2 = creditWeb
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

    private fun setSpannableString(
        fullText: String,
        highlightTargets: List<String>, // Text to color
        clickableTargets: Map<String, String>, // Text to make clickable → URL
        textView: TextView
    ) {
        val spannable = SpannableString(fullText)

        // Default text color
        spannable.setSpan(
            ForegroundColorSpan(Color.BLACK),
            0,
            fullText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Highlight colored targets
        highlightTargets.forEach { item ->
            val start = fullText.indexOf(item)
            if (start >= 0) {
                spannable.setSpan(
                    ForegroundColorSpan(Color.parseColor("#8246FF")),
                    start,
                    start + item.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        // Set clickable spans
        clickableTargets.forEach { (text, url) ->
            val start = fullText.indexOf(text)
            if (start >= 0) {
                val clickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        widget.context.startActivity(intent)
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.isUnderlineText = false // Optional: remove underline
                        ds.color = Color.parseColor("#8246FF") // Optional: match color
                    }
                }

                spannable.setSpan(
                    clickableSpan,
                    start,
                    start + text.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        textView.text = spannable
        textView.movementMethod = LinkMovementMethod.getInstance() // Required to make links work
        textView.highlightColor = Color.TRANSPARENT // Optional: avoid link highlight
    }

}