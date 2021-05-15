package com.udacity

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0

    private val valueAnimator = ValueAnimator()

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { p, old, new ->
    }

    // text
    private var text = resources.getString(R.string.button_loading)
    private val textPosition: PointF = PointF(0.0f, 0.0f)

    // circle
    private var radius = 0.0f
    private val circlePosition: PointF = PointF(0.0f, 0.0f)

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = resources.getDimension(R.dimen.default_text_size)
    }

    init {

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = (heightSize / 2.0f * 0.5).toFloat()
        textPosition.calculateTextPosition()
        circlePosition.calculateCirclePosition()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            paint.color = resources.getColor(R.color.white, context.theme)
            drawText(text, textPosition.x, textPosition.y, paint)
            paint.color = resources.getColor(R.color.colorAccent, context.theme)
            drawCircle(circlePosition.x, circlePosition.y, radius, paint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    private fun PointF.calculateTextPosition() {
        x = widthSize / 2.0f - radius
        y = heightSize / 2.0f + resources.getDimension(R.dimen.loading_button_text_size) / 2.0f
    }

    private fun PointF.calculateCirclePosition() {
        val bounds: Rect = Rect()
        paint.getTextBounds(text, 0, text.length, bounds)
        x = textPosition.x + bounds.width() / 2f + CIRCLE_OFFSET_X + radius
        y = heightSize / 2.0f
    }

    companion object {
        const val CIRCLE_OFFSET_X = 20f
    }

}