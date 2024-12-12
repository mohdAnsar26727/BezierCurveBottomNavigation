package com.example.beziercurvebottomnavigation.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.util.TypedValueCompat

class BezierView@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
)  : View(
    context, attrs, defStyleAttr, defStyleRes
) {

    private var mainPaint: Paint? = null
    private var shadowPaint: Paint? = null
    private var mainPath: Path? = null
    private var shadowPath: Path? = null
    val innerArray = Array(11) { PointF() }
    var progressArray = Array(11) { PointF() }

    private var width = 0f
    private var height = 0f
    private val shadowHeight =
        TypedValueCompat.dpToPx(35f, context.resources.displayMetrics)   // this height will change bottomnavigation bg height

    var color = 0
        set(value) {
            field = value
            mainPaint?.color = field
            invalidate()
        }
    var shadowColor = 0
        set(value) {
            field = value
            shadowPaint?.setShadowLayer(TypedValueCompat.dpToPx(2f, context.resources.displayMetrics), 0f, 0f, shadowColor)
            invalidate()
        }

    var bezierX = 0f
        set(value) {
            if (value == field) return
            field = value
            Log.e("BezierX", field.toString());
            invalidate()
        }

    var bezierCurveHeight = 0f
        set(value) {
            if (value == field) return
            field = value
            invalidate()
        }

    var bezierInnerWidth = 0f
        set(value) {
            if (value == field) return
            field = value
            invalidate()
        }

    var bezierInnerHeight = 0f
        set(value) {
            if (value == field) return
            field = value
            invalidate()
        }

    var progress = 0f
        set(value) {
            if (value == field) return
            field = value

            progressArray[1].x = bezierX - bezierInnerWidth / 2
            progressArray[2].x = bezierX - bezierInnerWidth / 4
            progressArray[3].x = bezierX - bezierInnerWidth / 4
            progressArray[4].x = bezierX
            progressArray[5].x = bezierX + bezierInnerWidth / 4
            progressArray[6].x = bezierX + bezierInnerWidth / 4
            progressArray[7].x = bezierX + bezierInnerWidth / 2
            for (i in 2..6) {
                if (progress <= 1f) {//convert to outer
                    progressArray[i].y = calculate(innerArray[i].y, innerArray[0].y)
                } else {
                    progressArray[i].y = calculate(innerArray[0].y, innerArray[i].y)
                }
            }
            if (field == 2f) field = 0f

            invalidate()
        }

    var radius: Float = 60f
        set(value) {
            field = value
            invalidate()
        }




    init {
        setWillNotDraw(false)

        mainPath = Path()
        shadowPath = Path()

        mainPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mainPaint?.apply {
            strokeWidth = 0f
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = this@BezierView.color
        }

        shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        shadowPaint?.apply {
            isAntiAlias = true
            setShadowLayer(TypedValueCompat.dpToPx(4f, context.resources.displayMetrics), 0f, 0f, shadowColor)
        }

        color = color
        shadowColor = shadowColor

        setLayerType(LAYER_TYPE_SOFTWARE, shadowPaint)
        calculateInner()
    }

    @SuppressLint("DrawAllocation")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        width = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        height = MeasureSpec.getSize(heightMeasureSpec).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mainPath?.reset()
        shadowPath?.reset()

        if (bezierX == 0f) return

        if (progress == 0f) {
            drawInner(canvas, true)
            drawInner(canvas, false)
        } else {
            drawProgress(canvas, true)
            drawProgress(canvas, false)
        }
    }

    private fun drawInner(canvas: Canvas, isShadow: Boolean) {
        val paint = if (isShadow) shadowPaint else mainPaint
        val path = if (isShadow) shadowPath else mainPath

        paint?.apply {
            strokeWidth = 2f
            isAntiAlias = true
            style = Paint.Style.FILL
            color = this@BezierView.color
        }
        calculateInner()
        path?.apply {
            // Move to the starting point
            moveTo(innerArray[0].x, innerArray[0].y + radius)

            // Top-left rounded corner
            quadTo(innerArray[0].x, innerArray[0].y, radius, innerArray[0].y)

            // Top edge
            lineTo(innerArray[1].x + radius, innerArray[1].y)

            // Other curve sections
            cubicTo(
                innerArray[2].x,
                innerArray[2].y,
                innerArray[3].x,
                innerArray[3].y,
                innerArray[4].x,
                innerArray[4].y
            )

            cubicTo(
                innerArray[5].x,
                innerArray[5].y,
                innerArray[6].x,
                innerArray[6].y,
                innerArray[7].x - radius,
                innerArray[7].y
            )

            // Right edge leading to the rounded top-right corner
            lineTo(innerArray[8].x - radius, innerArray[8].y)

            // Top-right rounded corner

            //moveTo(innerArray[8].x, innerArray[8].y)

            quadTo(
                innerArray[8].x, innerArray[8].y, innerArray[8].x, innerArray[8].y + radius
            )

            // Bottom edge
            lineTo(innerArray[9].x, innerArray[9].y)

            // Left edge
            lineTo(innerArray[10].x, innerArray[10].y)


            // Close the path
            close()
        }

        progressArray = innerArray.clone()
        path?.let { it1 ->
            paint?.let { it2 ->
                canvas.drawPath(it1, it2)
            }
        }
    }

    private fun calculateInner() {
        val extra = shadowHeight
        val pickHeight = height - bezierCurveHeight
        innerArray[0] = PointF(0f, bezierInnerHeight + extra)
        innerArray[1] = PointF((bezierX - bezierInnerWidth / 2), bezierInnerHeight + extra)
        innerArray[2] = PointF(bezierX - bezierInnerWidth / 4, bezierInnerHeight + extra)
        innerArray[3] = PointF(bezierX - bezierInnerWidth / 4, pickHeight)
        innerArray[4] = PointF(bezierX, pickHeight)
        innerArray[5] = PointF(bezierX + bezierInnerWidth / 4, pickHeight)
        innerArray[6] = PointF(bezierX + bezierInnerWidth / 4, bezierInnerHeight + extra)
        innerArray[7] = PointF(bezierX + bezierInnerWidth / 2, bezierInnerHeight + extra)
        innerArray[8] = PointF(width, bezierInnerHeight + extra)
        innerArray[9] = PointF(width, height)
        innerArray[10] = PointF(0f, height)
    }

    private fun drawProgress(canvas: Canvas, isShadow: Boolean) {
        val paint = if (isShadow) shadowPaint else mainPaint
        val path = if (isShadow) shadowPath else mainPath
        path?.apply {
            // Move to the starting point
            moveTo(progressArray[0].x, progressArray[0].y + radius)

            // Top-left rounded corner
            quadTo(progressArray[0].x, progressArray[0].y, radius, progressArray[0].y)

            // Top edge
            lineTo(progressArray[1].x + radius, progressArray[1].y)

            // Other curve sections
            cubicTo(
                progressArray[2].x,
                progressArray[2].y,
                progressArray[3].x,
                progressArray[3].y,
                progressArray[4].x,
                progressArray[4].y
            )

            cubicTo(
                progressArray[5].x,
                progressArray[5].y,
                progressArray[6].x,
                progressArray[6].y,
                progressArray[7].x - radius,
                progressArray[7].y
            )

            // Right edge leading to the rounded top-right corner
            lineTo(progressArray[8].x - radius, progressArray[8].y)

            // Top-right rounded corner

            //moveTo(progressArray[8].x, progressArray[8].y)

            quadTo(
                progressArray[8].x,
                progressArray[8].y,
                progressArray[8].x,
                progressArray[8].y + radius
            )

            // Bottom edge
            lineTo(progressArray[9].x, progressArray[9].y)

            // Left edge
            lineTo(progressArray[10].x, progressArray[10].y)


            // Close the path
            close()
        }
        path?.let { it1 ->
            paint?.let { it2 ->
                canvas.drawPath(it1, it2)
            }
        }

    }

    private fun calculate(start: Float, end: Float): Float {
        var p = progress
        if (p > 1f) p = progress - 1f
        if (p in 0.9f..1f) calculateInner()
        return (p * (end - start)) + start
    }
}