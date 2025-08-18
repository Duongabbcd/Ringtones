package com.ezt.ringify.ringtonewallpaper.screen.wallpaper.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.ezt.ringify.ringtonewallpaper.databinding.DialogAlarmBinding
import com.ezt.ringify.ringtonewallpaper.screen.ringtone.bottomsheet.SortBottomSheet.Companion.updateDisplayIcons

class AlarmDialog(
    context: Context,
    private val onClickOptionListener: (Long) -> Unit
) : Dialog(context) {
    private val binding by lazy { DialogAlarmBinding.inflate(layoutInflater) }

    init {
        setContentView(binding.root)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private var slideTime = 5000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            okBtn.setOnClickListener {
                onClickOptionListener(slideTime)
                dismiss()
            }


            fiveSecondOption.setOnClickListener {
                slideTime = 5000L
                updateDisplayIcons(
                    fiveSecondIcon,
                    listOf(
                        fifteenSecondIcon,
                        oneMinuteIcon,
                        fiveMinuteIcon,
                        oneHourIcon,
                        oneDayIcon
                    )
                )
            }

            fifteenSecondOption.setOnClickListener {
                slideTime = 15000L
                updateDisplayIcons(
                    fifteenSecondIcon,
                    listOf(fiveSecondIcon, oneMinuteIcon, fiveMinuteIcon, oneHourIcon, oneDayIcon)
                )
            }

            oneMinuteOption.setOnClickListener {
                slideTime = 60000L
                updateDisplayIcons(
                    oneMinuteIcon,
                    listOf(
                        fiveSecondIcon,
                        fifteenSecondIcon,
                        fiveMinuteIcon,
                        oneHourIcon,
                        oneDayIcon
                    )
                )
            }

            fiveMinuteOption.setOnClickListener {
                slideTime = 5 * 60000L
                updateDisplayIcons(
                    fiveMinuteIcon,
                    listOf(
                        fiveSecondIcon,
                        fifteenSecondIcon,
                        oneMinuteIcon,
                        oneHourIcon,
                        oneDayIcon
                    )
                )
            }

            oneHourOption.setOnClickListener {
                slideTime = 60 * 60000L
                updateDisplayIcons(
                    oneHourIcon,
                    listOf(
                        fiveSecondIcon,
                        fifteenSecondIcon,
                        oneMinuteIcon,
                        fiveMinuteIcon,
                        oneDayIcon
                    )
                )
            }

            oneDayOption.setOnClickListener {
                slideTime = 24 * 60 * 60000L
                updateDisplayIcons(
                    oneDayIcon,
                    listOf(
                        fiveSecondIcon,
                        fifteenSecondIcon,
                        oneMinuteIcon,
                        fiveMinuteIcon,
                        oneHourIcon
                    )
                )
            }

        }
    }

}