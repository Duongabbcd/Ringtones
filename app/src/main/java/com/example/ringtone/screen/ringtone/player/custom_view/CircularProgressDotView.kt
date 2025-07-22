package com.example.ringtone.screen.ringtone.player.custom_view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class CircularProgressDotView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#DDDDEE")  // light ring color
        style = Paint.Style.STROKE
        strokeWidth = 20f
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#5B00E8") // purple dot color
        style = Paint.Style.FILL
    }

    private var progress = 0f // 0 to 1 (fraction)

    fun setProgress(progressPercent: Int) {
        progress = progressPercent / 100f
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = (min(width, height) - ringPaint.strokeWidth) / 2f

        // Draw ring
        canvas.drawCircle(centerX, centerY, radius, ringPaint)

        // Calculate dot position on circumference
        val angle = 2 * Math.PI * progress - Math.PI / 2  // start at top
        val dotX = (centerX + radius * cos(angle)).toFloat()
        val dotY = (centerY + radius * sin(angle)).toFloat()

        val dotRadius = 15f

        // Draw dot
        canvas.drawCircle(dotX, dotY, dotRadius, dotPaint)
    }
}
