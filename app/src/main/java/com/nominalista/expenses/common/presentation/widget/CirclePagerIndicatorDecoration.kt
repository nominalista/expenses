package com.nominalista.expenses.common.presentation.widget

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// Solution from SO:
// https://stackoverflow.com/questions/33841363/how-to-make-a-page-indicator-for-horizontal-recyclerview

class CirclePagerIndicatorDecoration(
        context: Context,
        colorActiveResource: Int,
        colorInactiveResource: Int
) : RecyclerView.ItemDecoration() {

    private val colorActive = ContextCompat.getColor(context, colorActiveResource)
    private val colorInactive = ContextCompat.getColor(context, colorInactiveResource)

    private val indicatorHeight = DP * 8
    private val indicatorStrokeWidth = DP * 4
    private val indicatorItemLength = DP * 4
    private val indicatorItemPadding = DP * 8

    private val interpolator = AccelerateDecelerateInterpolator()
    private val paint = Paint()

    init {
        paint.strokeWidth = indicatorStrokeWidth
        paint.style = Paint.Style.STROKE
        paint.isAntiAlias = true
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val itemCount = parent.adapter?.itemCount ?: return

        // center horizontally, calculate width and subtract half from center
        val totalLength = indicatorItemLength * itemCount
        val paddingBetweenItems = Math.max(0, itemCount - 1) * indicatorItemPadding
        val indicatorTotalWidth = totalLength + paddingBetweenItems
        val indicatorStartX = (parent.width - indicatorTotalWidth) / 2f

        // center vertically in the allotted space
        val indicatorPosY = parent.height - indicatorHeight / 2f

        drawInactiveIndicators(c, indicatorStartX, indicatorPosY, itemCount)

        // find active page (which should be highlighted)
        val layoutManager = parent.layoutManager as LinearLayoutManager
        val activePosition = layoutManager.findFirstVisibleItemPosition()
        if (activePosition == RecyclerView.NO_POSITION) {
            return
        }

        // find offset of active page (if the user is scrolling)
        val activeChild = layoutManager.findViewByPosition(activePosition) ?: return
        val left = activeChild.left
        val width = activeChild.width

        // on swipe the active item will be positioned from [-width, 0]
        // interpolate offset for smooth animation
        val progress = interpolator.getInterpolation(left * -1 / width.toFloat())

        drawHighlights(c, indicatorStartX, indicatorPosY, activePosition, progress)
    }

    private fun drawInactiveIndicators(
            c: Canvas,
            indicatorStartX: Float,
            indicatorPosY: Float,
            itemCount: Int
    ) {
        paint.color = colorInactive

        // width of item indicator including padding
        val itemWidth = indicatorItemLength + indicatorItemPadding

        var start = indicatorStartX
        for (i in 0 until itemCount) {
            c.drawCircle(start, indicatorPosY, indicatorItemLength / 2f, paint)
            start += itemWidth
        }
    }

    private fun drawHighlights(
            c: Canvas,
            indicatorStartX: Float,
            indicatorPosY: Float,
            highlightPosition: Int,
            progress: Float
    ) {
        paint.color = colorActive

        // width of item indicator including padding
        val itemWidth = indicatorItemLength + indicatorItemPadding

        if (progress == 0F) {
            // no swipe, draw a normal indicator
            val highlightStart = indicatorStartX + itemWidth * highlightPosition;
            c.drawCircle(highlightStart, indicatorPosY, indicatorItemLength / 2F, paint)
        } else {
            val highlightStart = indicatorStartX + itemWidth * highlightPosition
            // calculate partial highlight
            val partialLength = indicatorItemLength * progress + indicatorItemPadding * progress
            c.drawCircle(highlightStart + partialLength,
                    indicatorPosY,
                    indicatorItemLength / 2F,
                    paint)
        }
    }

    override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.bottom = indicatorHeight.toInt()
    }

    companion object {
        private val DP = Resources.getSystem().displayMetrics.density
    }
}