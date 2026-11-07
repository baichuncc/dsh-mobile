package io.github.sayach.dshmobile

import android.content.Context
import android.graphics.Matrix
import android.widget.ImageView

/**
 * Cover-fitted room scene with a horizontal crop anchor. Phones keep the window/desk
 * slice while tablets see more of the room; the bitmap is only scaled, never stretched.
 */
internal class AyouSceneView(context: Context) : ImageView(context) {
    var anchorX: Float = 0.5f
        set(value) {
            field = value
            applyCover()
        }

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
        val scale = maxOf(width / imageWidth, height / imageHeight)
        val matrix = Matrix()
        matrix.setScale(scale, scale)
        matrix.postTranslate((width - imageWidth * scale) * anchorX, (height - imageHeight * scale) / 2f)
        imageMatrix = matrix
    }
}

/**
 * Character cutout placed by layout fractions. Height drives the scale; the width
 * budget clamps it so the figure is never stretched or blown past the screen.
 */
internal class AyouCharacterView(context: Context) : ImageView(context) {
    var heightFraction = 0.66f
    var centerXFraction = 0.5f
    var topFraction = 0.10f
    var maxWidthFactor = 1.06f

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
        var scale = height * heightFraction / imageHeight
        val widthBudget = width * maxWidthFactor
        if (imageWidth * scale > widthBudget) scale = widthBudget / imageWidth
        val matrix = Matrix()
        matrix.setScale(scale, scale)
        matrix.postTranslate(width * centerXFraction - imageWidth * scale / 2f, height * topFraction)
        imageMatrix = matrix
    }
}
