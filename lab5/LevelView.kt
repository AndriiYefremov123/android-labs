package com.example.lab5

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.*

class LevelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    var rollAngle: Float = 0f
    var pitchAngle: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    private val paintBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#16213e")
    }
    private val paintRing = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.parseColor("#0f3460")
    }
    private val paintHorizon = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = Color.parseColor("#4fc3f7")
    }
    private val paintBubble = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val paintCenter = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.parseColor("#546e7a")
    }
    private val paintTick = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.parseColor("#37474f")
    }
    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#90a4ae")
        textAlign = Paint.Align.CENTER
        textSize = 28f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val radius = min(cx, cy) * 0.88f

        canvas.drawCircle(cx, cy, radius, paintBg)
        canvas.drawCircle(cx, cy, radius, paintRing)

        // Cross lines
        paintTick.color = Color.parseColor("#263238")
        canvas.drawLine(cx - radius, cy, cx + radius, cy, paintTick)
        canvas.drawLine(cx, cy - radius, cx, cy + radius, paintTick)

        // Concentric rings
        for (r in listOf(0.25f, 0.5f, 0.75f)) {
            canvas.drawCircle(cx, cy, radius * r, paintCenter)
        }

        // Horizon line — rotates with roll
        canvas.save()
        canvas.rotate(rollAngle, cx, cy)
        val isLevel = abs(rollAngle) < 2f && abs(pitchAngle) < 2f
        paintHorizon.color = if (isLevel) Color.parseColor("#66bb6a")
        else Color.parseColor("#4fc3f7")
        paintHorizon.strokeWidth = if (isLevel) 8f else 5f
        canvas.drawLine(cx - radius * 0.85f, cy, cx + radius * 0.85f, cy, paintHorizon)
        canvas.restore()

        // Bubble position (clamped to circle)
        val maxOffset = radius * 0.7f
        val rawX = (rollAngle / 45f) * maxOffset
        val rawY = (pitchAngle / 45f) * maxOffset
        val dist = sqrt(rawX * rawX + rawY * rawY)
        val bx: Float
        val by: Float
        if (dist > maxOffset) {
            bx = cx + rawX / dist * maxOffset
            by = cy + rawY / dist * maxOffset
        } else {
            bx = cx + rawX
            by = cy + rawY
        }

        // Bubble glow
        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            shader = RadialGradient(
                bx, by, 35f,
                if (isLevel) Color.parseColor("#8866bb6a") else Color.parseColor("#884fc3f7"),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(bx, by, 35f, glowPaint)

        // Bubble
        paintBubble.color = if (isLevel) Color.parseColor("#66bb6a")
        else Color.parseColor("#4fc3f7")
        canvas.drawCircle(bx, by, 22f, paintBubble)
    }
}