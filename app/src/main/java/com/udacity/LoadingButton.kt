package com.udacity

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var widthSize = 0
    private var heightSize = 0

    // Styling attributes
    private var defaultBackgroundColor = 0
    private var loadingBackgroundColor = 0
    private var defaultText: CharSequence = ""
    private var loadingText: CharSequence = ""
    private var textColor = 0
    private var circleColor = 0

    private var buttonText = ""
    private lateinit var buttonTextBounds: Rect

    private var radius = 0.0f
    private val circleRect = RectF()

    private fun retrieveButtonTextBounds() {
        buttonTextBounds = Rect()
        paint.getTextBounds(buttonText, 0, buttonText.length, buttonTextBounds)
    }

    /**
     * Calculate circle rect.
     * It will be used to draw arc simulate loading circle by animation.
     */
    private fun calculateCircleRect() {
        val circleCenterX = buttonTextBounds.right + buttonTextBounds.width() + CIRCLE_MARGIN_LEFT + radius
        val circleCenterY = heightSize / 2.0f
        val left = circleCenterX - radius
        val top = circleCenterY - radius
        val right = circleCenterX + radius
        val bottom = circleCenterY + radius
        circleRect.set(left, top, right, bottom)
    }

    private val animatorSet = AnimatorSet().apply {
        duration = ANIM_DURATION
        disableViewDuringAnimation(this@LoadingButton)
    }

    private fun AnimatorSet.disableViewDuringAnimation(view: View) {
        doOnStart {
            view.isEnabled = false
        }
        doOnEnd {
            view.isEnabled = true
        }
    }

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Completed) { _, _, newState ->
        when (newState) {
            ButtonState.Loading -> {
                buttonText = loadingText.toString()
                if (!::buttonTextBounds.isInitialized) {
                    retrieveButtonTextBounds()
                    calculateCircleRect()
                }
                animatorSet.start()
            }
            else -> {
                buttonText = defaultText.toString()
            }
        }
    }

    private var currentCircleAnimationValue = 0.0f
    private val circleAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
//        repeatCount = 1
//        duration = ANIM_DURATION
        addUpdateListener {
            currentCircleAnimationValue = it.animatedValue as Float
            invalidate()
        }
    }

    private var currentBackgroundAnimationValue = 0f
    private lateinit var backgroundAnimator: ValueAnimator

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = resources.getDimension(R.dimen.default_text_size)
    }

    init {
        isClickable = true
        context.withStyledAttributes(attrs, R.styleable.LoadingButton) {
            defaultBackgroundColor =
                getColor(R.styleable.LoadingButton_defaultBackgroundColor, 0)
            loadingBackgroundColor =
                getColor(R.styleable.LoadingButton_loadingBackgroundColor, 0)
            defaultText =
                getText(R.styleable.LoadingButton_defaultText)
            loadingText =
                getText(R.styleable.LoadingButton_loadingText)
            textColor =
                getColor(R.styleable.LoadingButton_textColor, 0)
        }.also {
            buttonText = defaultText.toString()
            circleColor = ContextCompat.getColor(context, R.color.colorAccent)
        }
    }

    override fun performClick(): Boolean {
        super.performClick()
        if (buttonState == ButtonState.Completed) {
            buttonState = ButtonState.Clicked
            invalidate()
        }
        return true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        radius = heightSize / 2.0f * CIRCLE_SIZE_MULTIPLIER
        createBackgroundAnimator()
        animatorSet.playTogether(circleAnimator, backgroundAnimator)
    }

    private fun createBackgroundAnimator() {
        ValueAnimator.ofFloat(0f, widthSize.toFloat()).apply {
//            repeatCount = 1
//            duration = ANIM_DURATION
//            interpolator = LinearInterpolator()
            addUpdateListener {
                currentBackgroundAnimationValue = it.animatedValue as Float
                invalidate()
            }
        }.also {
            backgroundAnimator = it
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            drawBackground()
            drawButtonText()
            drawCircleIfLoading()
        }
    }

    private fun Canvas.drawBackground() {
        when (buttonState) {
            ButtonState.Loading -> {
                drawLoadingBackground()
                drawDefaultBackground()
            }
            else -> {
                drawColor(defaultBackgroundColor)
            }
        }
    }

    private fun Canvas.drawLoadingBackground() = paint.apply {
        color = loadingBackgroundColor
    }.run {
        drawRect(0f, 0f, currentBackgroundAnimationValue, heightSize.toFloat(), this)
    }

    private fun Canvas.drawDefaultBackground() = paint.apply {
        color = defaultBackgroundColor
    }.run {
        drawRect(0f, 0f, widthSize.toFloat(), heightSize.toFloat(), this)
    }

    private fun Canvas.drawButtonText() = paint.apply {
        color = textColor
    }.run {
        drawText(
            buttonText,
            widthSize / 2.0f,
            heightSize / 2.0f + ((descent() - ascent()) / 2.0f) - descent(),
            this
        )
    }

    private fun Canvas.drawCircleIfLoading() {
        buttonState.takeIf {
            it == ButtonState.Loading
        }?.let {
            paint.apply {
                color = circleColor
            }.run {
                drawArc(circleRect, -360f, currentCircleAnimationValue, true, this)
            }
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

    fun changeButtonState(state: ButtonState) {
        if(buttonState != state) {
            buttonState = state
            invalidate()
        }
    }

    companion object {
        private const val CIRCLE_SIZE_MULTIPLIER = 0.4f
        private const val CIRCLE_MARGIN_LEFT = 20f
        private const val ANIM_DURATION = 3_000L
    }

}