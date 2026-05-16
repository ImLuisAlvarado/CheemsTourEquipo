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
        strokeWidth = 5f
        color = Color.parseColor("#CCCCCC")
        pathEffect = DashPathEffect(floatArrayOf(15f, 10f), 0f)
    }

    private val baseLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        color = Color.parseColor("#CCCCCC")
    }

    private val celestialPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private var progress = 0f
    private var isDay = true
    private val arcRect = RectF()

    // Recibe los tiempos absolutos en formato UNIX (Segundos)
    fun setTimes(sunriseUnix: Long, sunsetUnix: Long, currentUnix: Long) {
        isDay = currentUnix in sunriseUnix..sunsetUnix

        // Matemáticas para progreso de Día o Noche
        progress = if (isDay) {
            val totalDay = sunsetUnix - sunriseUnix
            if (totalDay > 0) (currentUnix - sunriseUnix).toFloat() / totalDay.toFloat() else 0f
        } else {
            if (currentUnix < sunriseUnix) {
                // Es de madrugada (antes del amanecer)
                val prevSunset = sunsetUnix - 86400 // Atardecer del día anterior
                val totalNight = sunriseUnix - prevSunset
                if (totalNight > 0) (currentUnix - prevSunset).toFloat() / totalNight.toFloat() else 0f
            } else {
                // Es de noche (después del atardecer)
                val nextSunrise = sunriseUnix + 86400 // Amanecer de mañana
                val totalNight = nextSunrise - sunsetUnix
                if (totalNight > 0) (currentUnix - sunsetUnix).toFloat() / totalNight.toFloat() else 0f
            }
        }

        // Blindaje de seguridad
        if (progress < 0f) progress = 0f
        if (progress > 1f) progress = 1f

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val centerX = width / 2f
        val centerY = height - 30f // Espacio inferior para que no se corte

        // CORRECCIÓN CRÍTICA: El radio ahora respeta la altura disponible
        val radius = min(width / 2f, height) - 40f
        arcRect.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

        // Línea horizontal y arco
        canvas.drawLine(centerX - radius - 20f, centerY, centerX + radius + 20f, centerY, baseLinePaint)
        canvas.drawArc(arcRect, 180f, 180f, false, pathPaint)

        // Trigonometría para colocar el cuerpo celeste
        val angleDeg = 180f + (180f * progress)
        val angleRad = Math.toRadians(angleDeg.toDouble())

        val cx = centerX + radius * Math.cos(angleRad).toFloat()
        val cy = centerY + radius * Math.sin(angleRad).toFloat()

        if (isDay) {
            celestialPaint.color = Color.parseColor("#FFC107") // Sol (Amarillo)
            canvas.drawCircle(cx, cy, 25f, celestialPaint)
        } else {
            celestialPaint.color = Color.parseColor("#78909C") // Luna (Gris azulado)
            canvas.drawCircle(cx, cy, 20f, celestialPaint)
        }
    }
}