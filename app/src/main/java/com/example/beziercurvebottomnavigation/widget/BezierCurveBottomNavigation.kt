package com.example.beziercurvebottomnavigation.widget

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.LayoutTransition
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import androidx.core.util.TypedValueCompat
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.example.beziercurvebottomnavigation.R
import com.example.beziercurvebottomnavigation.databinding.NavigationItemBinding

class BezierCurveBottomNavigation
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    @DrawableRes
    private val navigationItems: MutableList<Int> = mutableListOf()

    private val bezierView: BezierView = BezierView(context)
    private val navigationItemLayout: LinearLayout = LinearLayout(context)

    private val listeners = mutableListOf<((Int) -> Unit)>()

    var navigationBackgroundColor: Int = Color.WHITE
        set(value) {
            field = value
            setBackgroundColor(navigationBackgroundColor)
        }

    var selectedIcon = 0
        private set(value) {
            field = value
            listeners.forEach { it(value) }
        }

    var animDuration = 1000

    var selectedIconColor = Color.BLACK
        set(value) {
            field = value
            invalidate()
        }

    var iconColor = Color.BLACK
        set(value) {
            field = value
            invalidate()
        }

    var bezierInnerWidthOffset = 0f
        set(value) {
            field = value
            initializeNavigationItemViews()
        }

    override fun setBackgroundColor(color: Int) {
        bezierView.color = color
    }

    init {
        val typedArray = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.BezierCurveBottomNavigation,
            0,
            0
        )

        runCatching {
            with(typedArray) {
                iconColor = getColor(
                    R.styleable.BezierCurveBottomNavigation_iconColor,
                    iconColor
                )
                selectedIconColor = getColor(
                    R.styleable.BezierCurveBottomNavigation_selectedIconColor,
                    iconColor
                )
                navigationBackgroundColor = getColor(
                    R.styleable.BezierCurveBottomNavigation_backgroundColor,
                    Color.WHITE
                )
                animDuration = getInteger(
                    R.styleable.BezierCurveBottomNavigation_animationDuration,
                    500
                )

                bezierView.apply {
                    bezierCurveHeight =
                        getDimension(R.styleable.BezierCurveBottomNavigation_bezierCurveHeight, 0f)
                    bezierInnerHeight =
                        getDimension(R.styleable.BezierCurveBottomNavigation_bezierInnerHeight, 0f)

                    bezierInnerWidthOffset = getDimension(
                        R.styleable.BezierCurveBottomNavigation_bezierInnerWidthOffset,
                        0.0f
                    )
                }

                recycle()
            }
        }

        removeAllViews()

        val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        bezierView.layoutParams = layoutParams
        navigationItemLayout.layoutParams = layoutParams

        addView(bezierView)
        addView(navigationItemLayout)

        navigationItemLayout.setPadding(
            bezierView.radius.toInt(),
            0,
            bezierView.radius.toInt(),
            0
        )

        navigationItemLayout.layoutTransition = LayoutTransition().apply {
            enableTransitionType(LayoutTransition.CHANGING)
        }

        bezierView.apply {
            color = navigationBackgroundColor
            shadowColor = Color.GRAY
        }
        initializeNavigationItemViews()
    }

    fun onItemSelection(listener: (Int) -> Unit) {
        listeners.add(listener)
    }

    fun setNavigationItems(@DrawableRes items: List<Int>, selected: Int) {
        selectedIcon = selected
        navigationItems.clear()
        navigationItems.addAll(items)
        initializeNavigationItemViews()
    }


    private fun initializeNavigationItemViews() {
        with(navigationItemLayout) {
            removeAllViews()

            navigationItems.forEach { iconRes ->
                with(NavigationItemBinding.inflate(LayoutInflater.from(context))) {
                    root.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1f
                    )

                    with(iconIv) {
                        setImageResource(iconRes)
                        setColorFilter(selectedIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
                    }

                    addView(root)

                    root.post {
                        iconIv.updateLayoutParams<LayoutParams> {
                            width = root.measuredHeight / 2
                            height = root.measuredHeight / 2
                        }
                        onSelection(
                            bezierView.bezierX == 0f && selectedIcon == iconRes,
                            animate = false
                        )
                    }

                    onItemSelection {
                        onSelection(iconRes == selectedIcon)
                    }

                    root.setOnClickListener {
                        if (iconRes == selectedIcon) return@setOnClickListener
                        selectedIcon = iconRes
                        onSelection(true)
                    }
                }
            }

            post {
                bezierView.apply {
                    bezierInnerWidth =
                        navigationItemLayout.measuredHeight + TypedValueCompat.dpToPx(16f, resources.displayMetrics)// / (navigationItems.size - bezierInnerWidthOffset)

                    if (bezierView.bezierInnerHeight == 0f
                        || bezierView.bezierCurveHeight == 0f
                    ) {
                        bezierInnerHeight = navigationItemLayout.measuredHeight * 0.03f
                        bezierCurveHeight = navigationItemLayout.measuredHeight * 0.34f
                    }
                }.post {
                    listeners.forEach { it(selectedIcon) }
                }
            }
        }
    }


    private fun anim(newX: Float) {
        val animInterpolator = FastOutSlowInInterpolator()

        val anim = ValueAnimator.ofFloat(0f, 1f)
        anim.apply {
            duration = animDuration.toLong()
            interpolator = animInterpolator
            val beforeX = bezierView.bezierX
            addUpdateListener {
                val f = it.animatedFraction

                if (newX > beforeX)
                    bezierView.bezierX = f * (newX - beforeX) + beforeX
                else
                    bezierView.bezierX = beforeX - f * (beforeX - newX)

            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {

                }
            })
            start()
        }

        val progressAnim = ValueAnimator.ofFloat(0f, 1f)
        progressAnim.apply {
            duration = animDuration.toLong()
            interpolator = animInterpolator
            addUpdateListener {
                val f = it.animatedFraction
                bezierView.progress = f * 2f
            }
            start()
        }
    }

    private fun NavigationItemBinding.onSelection(isSelected: Boolean, animate: Boolean = true) {

        val bezierTop = bezierView.innerArray.first().y // Top Y value of the Bezier curve
        val bezierBottom = bezierView.innerArray.last().y // Bottom Y value of the Bezier curve


        if (isSelected) {
            // When selected, animate the icon to a different margin (move upwards or make it larger)
            val selectedTopMargin = root.height / 8 // Example for selected state

            if (animate) {
                // Animate topMargin change when selected
                val animator = ValueAnimator.ofInt(iconIv.marginTop, selectedTopMargin)
                animator.duration = 300 // Set duration for animation (you can adjust as needed)
                animator.addUpdateListener { animation ->
                    val params = iconIv.layoutParams as LayoutParams
                    params.topMargin = animation.animatedValue as Int
                    iconIv.layoutParams = params
                }
                animator.start()
            } else {
                // If no animation, directly set the margin
                iconIv.updateLayoutParams<LayoutParams> {
                    gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
                    topMargin = selectedTopMargin
                    bottomMargin = 0
                }
            }

            // Center horizontally and change background color when selected
            val childX = root.left + (root.width / 2) // Center of the child horizontally
            if (animate) anim(childX.toFloat()) else bezierView.bezierX = childX.toFloat()

        } else {
            // When unselected, calculate and animate the center position
            val bezierHeight = bezierBottom - bezierTop
            val iconHeight = iconIv.height

            // Calculate the center position of the icon within the Bezier bounds
            val verticalCenter = bezierTop + (bezierHeight - iconHeight) / 2

            if (animate) {
                // Animate topMargin change when unselected
                val animator = ValueAnimator.ofInt(iconIv.marginTop, verticalCenter.toInt())
                animator.duration = 300 // Set duration for animation (you can adjust as needed)
                animator.addUpdateListener { animation ->
                    val params = iconIv.layoutParams as LayoutParams
                    params.topMargin = animation.animatedValue as Int
                    iconIv.layoutParams = params
                }
                animator.start()
            } else {
                // If no animation, directly set the margin
                iconIv.updateLayoutParams<LayoutParams> {
                    gravity = Gravity.CENTER_HORIZONTAL
                    topMargin = verticalCenter.toInt()
                    bottomMargin = 0
                }
            }
        }

        // Apply background color change regardless of selected state
        iconIv.backgroundTintList = ColorStateList.valueOf(navigationBackgroundColor)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        listeners.clear()
    }

}