@file:Suppress("UNUSED")

package top.e404.skiko.draw.element

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Paint
import org.jetbrains.skia.RRect
import org.jetbrains.skia.Rect
import top.e404.skiko.util.Colors
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
    var lineWeight: Float,
    var udSpacing: Float,
    var lrSpacing: Float,
    var spacingColor: Int = Colors.WHITE.argb,
) : DrawElement {
    override fun size(minWidth: Int, maxWidth: Int): Pair<Float, Float> {
        return 0F to lineWeight + udSpacing * 2
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
                l = imagePadding + lrSpacing,
                t = pointer.y + udSpacing,
                w = width - lrSpacing * 2,
                h = lineWeight,
                radius = udSpacing / 2
            ), paint.apply {
                color = spacingColor
            }
        )
        if (debug) canvas.drawRect(
            Rect.makeXYWH(
                l = imagePadding + lrSpacing,
                t = pointer.y + udSpacing,
                w = width - lrSpacing * 2,
                h = lineWeight,
            ), debugPaint
        )
        pointer.y += lineWeight + udSpacing * 2
    }
}
