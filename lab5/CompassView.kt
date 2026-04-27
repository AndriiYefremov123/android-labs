package com.example.lab5

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.*

class CompassView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    var azimuth: Float = 0f
        set(value) {
            field = value
            invalidate()
        }

    private val paintCircle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#16213e")
    }
    private val paintRing = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = Color.parseColor("#0f3460")
    }
    private val paintNeedle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#e0e0e0")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val paintCardinal = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val paintTick = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
    }
    private val paintCenter = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#e0e0e0")
    }
    private val paintGlow = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.parseColor("#334fc3f7")
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val radius = min(cx, cy) * 0.9f

        // Background circle
        canvas.drawCircle(cx, cy, radius, paintCircle)
        canvas.drawCircle(cx, cy, radius, paintRing)
        canvas.drawCircle(cx, cy, radius * 0.95f, paintGlow)

        // Tick marks
        for (i in 0 until 360 step 5) {
            val isMajor = i % 45 == 0
            val isMedium = i % 15 == 0
            val tickLen = when {
                isMajor -> radius * 0.12f
                isMedium -> radius * 0.07f
                else -> radius * 0.04f
            }
            paintTick.strokeWidth = if (isMajor) 3f else 1.5f
            paintTick.color = if (isMajor) Color.parseColor("#4fc3f7") else Color.parseColor("#37474f")

            val angleRad = Math.toRadians(i.toDouble()).toFloat()
            val startX = cx + (radius * 0.88f) * sin(angleRad)
            val startY = cy - (radius * 0.88f) * cos(angleRad)
            val stopX = cx + (radius * 0.88f - tickLen) * sin(angleRad)
            val stopY = cy - (radius * 0.88f - tickLen) * cos(angleRad)
            canvas.drawLine(startX, startY, stopX, stopY, paintTick)
        }

        // Cardinal directions (fixed, compass rotates via needle)
        val cardinals = listOf("N" to 0f, "E" to 90f, "S" to 180f, "W" to 270f)
        paintCardinal.textSize = radius * 0.13f
        for ((label, angle) in cardinals) {
            val rad = Math.toRadians(angle.toDouble()).toFloat()
            val tx = cx + radius * 0.68f * sin(rad)
            val ty = cy - radius * 0.68f * cos(rad) + paintCardinal.textSize / 3
            paintCardinal.color = if (label == "N") Color.parseColor("#ef5350")
            else Color.parseColor("#90a4ae")
            canvas.drawText(label, tx, ty, paintCardinal)
        }

        // Degree labels
        paintText.textSize = radius * 0.07f
        for (deg in listOf(0, 90, 180, 270)) {
            // skipped — cardinals cover these
        }

        // NEEDLE (rotates with azimuth) ===
        canvas.save()
        canvas.rotate(-azimuth, cx, cy)

        val needleLen = radius * 0.55f
        val needleWidth = radius * 0.06f

        // North (red)
        paintNeedle.color = Color.parseColor("#ef5350")
        val northPath = Path().apply {
            moveTo(cx, cy - needleLen)
            lineTo(cx - needleWidth, cy)
            lineTo(cx + needleWidth, cy)
            close()
        }
        canvas.drawPath(northPath, paintNeedle)

        // South (white/grey)
        paintNeedle.color = Color.parseColor("#b0bec5")
        val southPath = Path().apply {
            moveTo(cx, cy + needleLen * 0.75f)
            lineTo(cx - needleWidth, cy)
            lineTo(cx + needleWidth, cy)
            close()
        }
        canvas.drawPath(southPath, paintNeedle)

        canvas.restore()

        // Center dot
        canvas.drawCircle(cx, cy, radius * 0.055f, paintCenter)
        paintNeedle.color = Color.parseColor("#1a1a2e")
        canvas.drawCircle(cx, cy, radius * 0.025f, paintNeedle)
    }
}