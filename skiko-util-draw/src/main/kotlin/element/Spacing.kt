@file:Suppress("UNUSED")

package top.e404.skiko.draw.element

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Paint
import top.e404.skiko.draw.DrawElement
import top.e404.skiko.draw.Pointer

/**
 * 代表一个占位对象(用于控制组件间距离)
 *
 * @property height 高度
 */
open class Spacing(var height: Float) : DrawElement {
    override fun size(minWidth: Int, maxWidth: Int) = 0F to height

    override fun drawToBoard(
        canvas: Canvas,
        pointer: Pointer,
        paint: Paint,
        width: Int,
        imagePadding: Int,
        debug: Boolean
    ) {
        pointer.y += height
    }
}