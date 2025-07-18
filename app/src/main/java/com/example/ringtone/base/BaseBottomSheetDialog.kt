package com.example.ringtone.base

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewbinding.ViewBinding
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlin.let
import kotlin.run

abstract class BaseBottomSheetDialog<VB : ViewBinding>(context: Context) : BottomSheetDialog(context,
    com.example.ringtone.R.style.ThemeBottomDialog
) {
    val binding: VB by lazy { getViewBinding() }

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        try {
            hideNavigationBar()
        }catch (e: Exception){

        }
        setWhiteNavigationBar(this)
        createContentView()
    }

    private fun hideNavigationBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window?.let { WindowCompat.setDecorFitsSystemWindows(it, false) }
            window?.let {
                WindowInsetsControllerCompat(it, window!!.decorView).let { controller ->
                    controller.hide(WindowInsetsCompat.Type.systemBars())
                    controller.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            }
        } else {
            hideSystemUIBeloR()
        }
    }


    private fun hideSystemUIBeloR() {
        val decorView: View = window?.decorView!!
        val uiOptions = decorView.systemUiVisibility
        var newUiOptions = uiOptions
        newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_LOW_PROFILE
        newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_IMMERSIVE
        newUiOptions = newUiOptions or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.systemUiVisibility = newUiOptions
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        val bottomSheet = findViewById<View>(R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.isFitToContents = true
            behavior.skipCollapsed = true
            behavior.isHideable = true
            it.layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT

            // Handle keyboard visibility changes
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//                window?.let { window ->
//                    WindowCompat.setDecorFitsSystemWindows(window, false)
//                    window.decorView.setOnApplyWindowInsetsListener { _, insets ->
//                        val keyboardHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
//                        if (keyboardHeight > 0) {
//                            // Keyboard is visible, adjust bottom sheet
//                            behavior.isFitToContents = false
//                            behavior.peekHeight = keyboardHeight
//                        } else {
//                            // Keyboard is hidden, reset bottom sheet
//                            behavior.isFitToContents = true
//                            behavior.peekHeight = 0
//                        }
//                        insets
//                    }
//                }
//            }

            it.post {
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

//        window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
//        window?.setGravity(Gravity.BOTTOM)
    }

    private fun createContentView() {
        setContentView(binding.root)
    }

    abstract fun getViewBinding(): VB

    abstract fun initViews()


    fun setDialogBottom() {
        window?.run {
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            setGravity(Gravity.BOTTOM)
        }
    }

    private fun setWhiteNavigationBar(dialog: Dialog) {
        val window: Window? = dialog.window
        if (window != null) {
            val metrics = DisplayMetrics()
            window.windowManager.defaultDisplay.getMetrics(metrics)
            val dimDrawable = GradientDrawable()
            // ...customize your dim effect here
            val navigationBarDrawable = GradientDrawable()
            navigationBarDrawable.shape = GradientDrawable.RECTANGLE
            navigationBarDrawable.setColor(Color.parseColor("#27242D"))
            val layers = arrayOf<Drawable>(dimDrawable, navigationBarDrawable)
            val windowBackground = LayerDrawable(layers)
            windowBackground.setLayerInsetTop(1, metrics.heightPixels)
            window.setBackgroundDrawable(windowBackground)
        }
    }
}