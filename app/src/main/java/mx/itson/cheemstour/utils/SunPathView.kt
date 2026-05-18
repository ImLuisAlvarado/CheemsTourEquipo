package mx.itson.cheemstour.utils

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class SunPathView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val pathPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.parseColor("#88FFFFFF") // Blanco semitransparente
        pathEffect = DashPathEffect(floatArrayOf(15f, 15f), 0f)
    }

    private val baseLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.parseColor("#88FFFFFF")
    }

    private val celestialPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private var progress = 0f
    private var isDay = true
    private val arcRect = RectF()

    fun setTimes(sunriseUnix: Long, sunsetUnix: Long, currentUnix: Long) {
        isDay = currentUnix in sunriseUnix..sunsetUnix

        if (isDay) {
            val totalDay = sunsetUnix - sunriseUnix
            progress = if (totalDay > 0) (currentUnix - sunriseUnix).toFloat() / totalDay else 0f
        } else {
            // Lógica simulada para la noche
            val dayDuration = sunsetUnix - sunriseUnix
            val nightDuration = 86400L - dayDuration

            if (currentUnix < sunriseUnix) {
                // Madrugada: la noche empezó ayer
                val nightStart = sunsetUnix - 86400L
                progress = if (nightDuration > 0) (currentUnix - nightStart).toFloat() / nightDuration else 0f
            } else {
                // Noche: la noche empezó hoy
                progress = if (nightDuration > 0) (currentUnix - sunsetUnix).toFloat() / nightDuration else 0f
            }
        }

        if (progress < 0f) progress = 0f
        if (progress > 1f) progress = 1f

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val centerX = width / 2f
        val centerY = height - 30f

        val radius = min(width / 2f, height) - 40f
        arcRect.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

        canvas.drawLine(centerX - radius - 20f, centerY, centerX + radius + 20f, centerY, baseLinePaint)
        canvas.drawArc(arcRect, 180f, 180f, false, pathPaint)

        val angleDeg = 180f + (180f * progress)
        val angleRad = Math.toRadians(angleDeg.toDouble())

        val cx = centerX + radius * Math.cos(angleRad).toFloat()
        val cy = centerY + radius * Math.sin(angleRad).toFloat()

        if (isDay) {
            celestialPaint.color = Color.parseColor("#FFD54F") // Sol
            canvas.drawCircle(cx, cy, 25f, celestialPaint)
        } else {
            celestialPaint.color = Color.parseColor("#E0E0E0") // Luna
            canvas.drawCircle(cx, cy, 22f, celestialPaint)
        }
    }
}