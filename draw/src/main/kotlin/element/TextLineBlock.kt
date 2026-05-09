@file:Suppress("UNUSED")

package top.e404.skiko.draw.element

import org.jetbrains.skia.*
import top.e404.skiko.util.Colors
import top.e404.skiko.draw.DrawElement
import top.e404.skiko.draw.Pointer
import top.e404.skiko.draw.splitByWidth

/**
 * 代表一个带色块背景的文本对象(类似按钮)
 *
 * @property content 文本
 * @property lineSpace 文本的行间距
 * @property font 字体
 * @property color 颜色
 * @property bgColor 背景颜色
 * @property bgRadius 背景圆角
 * @property padding 背景块距离内部文字的边距
 * @property margin 背景外边距
 * @property center 居中
 */
open class TextLineBlock(
    var content: String,
    var lineSpace: Int = 20,
    var font: Font,
    var color: Int = Colors.WHITE.argb,
    val bgColor: Int = Colors.LIGHT_BLUE.argb,
    val bgRadius: Float = 20F,
    var padding: Int = 20,
    var margin: Int = 20,
    var center: Boolean = true,
) : DrawElement {
    private var lines = listOf<TextLine>()
    var w = 0F
    var h = 0F

    override fun size(minWidth: Int, maxWidth: Int): Pair<Float, Float> {
        val (lines, width) = content.splitByWidth(maxWidth - (padding + margin) * 2, font, padding)
        this.lines = lines
        w = width
        h = lines.map { it.descent - it.ascent }.sum() + (lines.size - 1) * lineSpace
        return width + (padding + margin) * 2 to h + (margin + padding) * 2F
    }

    override fun drawToBoard(
        canvas: Canvas,
        pointer: Pointer,
        paint: Paint,
        width: Int,
        imagePadding: Int,
        debug: Boolean
    ) {
        // bg
        canvas.drawRRect(RRect.makeXYWH(
            if (center) (width + 2 * imagePadding - w - margin - padding) / 2F
            else pointer.x + margin,
            pointer.y + margin,
            w + 2F * padding,
            h + 2F * padding,
            bgRadius
        ), paint.apply { color = bgColor })
        // text
        pointer.y += padding + margin
        for (line in lines) {
            pointer.y -= line.ascent
            canvas.drawTextLine(
                line = line,
                x = if (center) (width + 2 * imagePadding - line.width) / 2
                else pointer.x + padding + margin,
                y = pointer.y,
                paint = paint.also { it.color = color }
            )
            pointer.y += line.descent + lineSpace
        }
        pointer.y += padding + margin - lineSpace
    }
}
