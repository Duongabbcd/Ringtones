package com.ezt.ringify.ringtonewallpaper.screen.ringtone.layout

import alirezat775.lib.carouselview.CarouselView
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.ezt.ringify.ringtonewallpaper.R

class TouchInterceptorLayout(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        // Only allow touch events to be dispatched to the CarouselView
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val carouselView = findViewById<CarouselView>(R.id.horizontalRingtones)
            if (carouselView != null && !isTouchInsideView(carouselView, ev)) {
                return true // Consume the touch event
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun isTouchInsideView(view: View, event: MotionEvent): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val rect =
            Rect(location[0], location[1], location[0] + view.width, location[1] + view.height)
        return rect.contains(event.rawX.toInt(), event.rawY.toInt())
    }
}
