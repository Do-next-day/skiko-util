@file:Suppress("UNUSED")

package top.e404.skiko.draw.element

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Paint
import org.jetbrains.skia.RRect
import org.jetbrains.skia.Rect
import top.e404.skiko.Colors
import top.e404.skiko.draw.DrawElement
import top.e404.skiko.draw.DrawElement.Companion.debugPaint
import top.e404.skiko.draw.Pointer

/**
 * 分割线
 *
 * @property lineWeight 线宽
 * @property udSpacing 上线边距
 * @property lrSpacing 左右边距
 * @property spacingColor 颜色
 */
open class SpacingLine(
    var lineWeight: Int,
    var udSpacing: Int,
    var lrSpacing: Int,
    var spacingColor: Int = Colors.WHITE.argb,
) : DrawElement {
    override fun size(minWidth: Int, maxWidth: Int): Pair<Int, Int> {
        return Pair(0, lineWeight + udSpacing * 2)
    }

    override fun drawToBoard(
        canvas: Canvas,
        pointer: Pointer,
        paint: Paint,
        width: Int,
        imagePadding: Int,
        debug: Boolean
    ) {
        canvas.drawRRect(
            RRect.makeXYWH(
                l = imagePadding + lrSpacing.toFloat(),
                t = pointer.y + udSpacing.toFloat(),
                w = width - lrSpacing * 2F,
                h = lineWeight.toFloat(),
                radius = udSpacing / 2F
            ), paint.apply {
                color = spacingColor
            }
        )
        if (debug) canvas.drawRect(
            Rect.makeXYWH(
                l = imagePadding + lrSpacing.toFloat(),
                t = pointer.y + udSpacing.toFloat(),
                w = width - lrSpacing * 2F,
                h = lineWeight.toFloat(),
            ), debugPaint
        )
        pointer.y += lineWeight + udSpacing * 2
    }
}