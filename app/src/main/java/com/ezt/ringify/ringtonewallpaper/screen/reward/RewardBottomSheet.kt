package com.ezt.ringify.ringtonewallpaper.screen.reward

import android.content.Context
import com.ezt.ringify.ringtonewallpaper.base.BaseBottomSheetDialog
import com.ezt.ringify.ringtonewallpaper.databinding.BottomSheetRewardBinding
import com.ezt.ringify.ringtonewallpaper.databinding.BottomSheetRingtoneDownloadBinding

class RewardBottomSheet(private val context: Context, private val onClickListener: () -> Unit) : BaseBottomSheetDialog<BottomSheetRewardBinding>(context) {
    override fun getViewBinding(): BottomSheetRewardBinding {
        return BottomSheetRewardBinding.inflate(layoutInflater)
    }

    override fun initViews() {
        setContentView(binding.root)

        binding.apply {
            rewardBtn.setOnClickListener {
                onClickListener()
                dismiss()
            }
        }
    }
}