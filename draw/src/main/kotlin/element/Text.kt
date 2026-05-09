@file:Suppress("UNUSED")

package top.e404.skiko.draw.element

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Font
import org.jetbrains.skia.Paint
import org.jetbrains.skia.TextLine
import top.e404.skiko.draw.DrawElement
import top.e404.skiko.draw.Pointer
import top.e404.skiko.draw.splitByWidth
import top.e404.skiko.util.Colors

/**
 * 代表一个文本对象
 *
 * @property content 文本
 * @property font 字体
 * @property color 字体颜色
 * @property udPadding 行间距, 上一个元素到首行以及末行到下一个元素的间距为1/2行间距
 * @property left 左侧边距
 * @property textIndent 若为true则启用行首缩进(两个空格的宽度)
 * @property center 居中, 若居中则忽略left
 */
open class Text(
    var content: String,
    var font: Font,
    var color: Int = Colors.WHITE.argb,
    var udPadding: Int = 20,
    var left: Int = 0,
    var textIndent: Boolean = false,
    var center: Boolean = true
) : DrawElement {
    private var lines = listOf<TextLine>()
    override fun size(minWidth: Int, maxWidth: Int): Pair<Float, Float> {
        var text = content
        if (!center && textIndent) text = "　　$text"
        val left = if (center) 0 else left
        val (lines, width) = text.splitByWidth(maxWidth - left, font, left)
        this.lines = lines
        val height = lines.map { it.descent - it.ascent }.sum() + lines.size * udPadding
        return width + left to height
    }

    override fun drawToBoard(
        canvas: Canvas,
        pointer: Pointer,
        paint: Paint,
        width: Int,
        imagePadding: Int,
        debug: Boolean
    ) {
        pointer.y -= udPadding / 2
        for (line in lines) {
            pointer.y += udPadding - line.ascent
            canvas.drawTextLine(
                line = line,
                x = if (center) (width + imagePadding * 2 - line.width) / 2
                else pointer.x + left,
                y = pointer.y,
                paint = paint.also { it.color = color }
            )
            pointer.y += line.descent
        }
        pointer.y += udPadding / 2
    }
}
