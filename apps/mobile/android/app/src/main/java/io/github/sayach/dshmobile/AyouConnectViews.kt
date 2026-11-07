package io.github.sayach.dshmobile

import android.content.Context
import android.graphics.Matrix
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout

/**
 * Cover-fitted room scene with a horizontal crop anchor. Phones keep the window/desk
 * slice while tablets see more of the room; the bitmap is only scaled, never stretched.
 * The anchor is recomputed on every size change, so rotation stays correct without
 * rebuilding the activity.
 */
internal class AyouSceneView(context: Context) : ImageView(context) {
    init {
        scaleType = ScaleType.MATRIX
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        applyCover()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        applyCover()
    }

    private fun applyCover() {
        val d = drawable ?: return
        if (width <= 0 || height <= 0) return
        val imageWidth = d.intrinsicWidth.toFloat()
        val imageHeight = d.intrinsicHeight.toFloat()
        if (imageWidth <= 0f || imageHeight <= 0f) return
        val landscape = width > height
        val tablet = resources.configuration.smallestScreenWidthDp >= 600
        val anchor = when {
            landscape -> 0.5f
            tablet -> 0.18f
            else -> 0.22f
        }
        val scale = maxOf(width / imageWidth, height / imageHeight)
        val matrix = Matrix()
        matrix.setScale(scale, scale)
        matrix.postTranslate((width - imageWidth * scale) * anchor, (height - imageHeight * scale) / 2f)
        imageMatrix = matrix
    }
}

/**
 * Character cutout placed by layout fractions. Height drives the scale; the width budget
 * clamps it so the figure is never stretched. Fractions follow the current orientation.
 */
internal class AyouCharacterView(context: Context) : ImageView(context) {
    init {
        scaleType = ScaleType.MATRIX
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        applyPlacement()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        applyPlacement()
    }

    private fun applyPlacement() {
        val d = drawable ?: return
        if (width <= 0 || height <= 0) return
        val imageWidth = d.intrinsicWidth.toFloat()
        val imageHeight = d.intrinsicHeight.toFloat()
        if (imageWidth <= 0f || imageHeight <= 0f) return
        val landscape = width > height
        val tablet = resources.configuration.smallestScreenWidthDp >= 600
        val heightFraction = when {
            landscape -> 0.86f
            tablet -> 0.66f
            else -> 0.70f
        }
        val centerX = if (landscape) 0.25f else 0.5f
        val top = if (landscape) 0.07f else 0.10f
        var scale = height * heightFraction / imageHeight
        val widthBudget = width * 1.06f
        if (imageWidth * scale > widthBudget) scale = widthBudget / imageWidth
        val matrix = Matrix()
        matrix.setScale(scale, scale)
        matrix.postTranslate(width * centerX - imageWidth * scale / 2f, height * top)
        imageMatrix = matrix
    }
}

/**
 * Places the single UI stack at the bottom-centre in portrait and at the right-centre in
 * landscape. Re-evaluated on every layout so a rotation keeps the composition correct.
 */
internal class AyouConnectStackHost(context: Context) : FrameLayout(context) {
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val child = getChildAt(0) ?: return
        val viewWidth = right - left
        val viewHeight = bottom - top
        val landscape = viewWidth > viewHeight
        (child as? LinearLayout)?.gravity =
            if (landscape) Gravity.START else Gravity.CENTER_HORIZONTAL
        val childWidth = child.measuredWidth
        val childHeight = child.measuredHeight
        val x = if (landscape) viewWidth - paddingRight - childWidth else (viewWidth - childWidth) / 2
        val y = if (landscape) (viewHeight - childHeight) / 2 else viewHeight - paddingBottom - childHeight
        child.layout(x, y, x + childWidth, y + childHeight)
    }
}
