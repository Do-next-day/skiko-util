@file:Suppress("UNUSED")

package top.e404.skiko.draw.element

import org.jetbrains.skia.*
import top.e404.skiko.Colors
import top.e404.skiko.draw.DrawElement
import top.e404.skiko.draw.Pointer
import top.e404.skiko.draw.splitByWidth

/**
 * 代表一个带背景的文本对象
 *
 * @property content 文本
 * @property lineSpace 文本的行间距
 * @property font 字体
 * @property color 颜色
 * @property bgColor 背景颜色
 * @property bgRadius 背景圆角
 * @property padding 背景内边距
 * @property margin 背景外边距
 * @property center 居中
 * @property offset 偏移调整
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
    var offset: Int = 10,
) : DrawElement {
    private var lines = listOf<TextLine>()
    var width = 0
    var height = 0

    override fun size(minWidth: Int, maxWidth: Int): Pair<Int, Int> {
        val (lines, width) = content.splitByWidth(maxWidth - (padding + margin) * 2, font, padding)
        this.lines = lines
        this.width = width + 2 * padding
        height = lines.size * (font.size.toInt() + lineSpace) - lineSpace
        return Pair(width + (padding + margin) * 2, height + (margin + padding) * 2)
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
            if (center) (width + 2 * imagePadding - this.width - margin - this.padding) / 2F
            else pointer.x + margin.toFloat(),
            pointer.y + margin.toFloat(),
            this.width + 2F * this.padding,
            (height + 2 * this.padding).toFloat(),
            bgRadius
        ), paint.apply { color = bgColor })
        // text
        pointer.y -= offset
        pointer.y += this.padding + margin
        for (line in lines) {
            pointer.y += font.size.toInt()
            canvas.drawTextLine(
                line,
                if (center) (width + 2 * imagePadding - line.width) / 2
                else pointer.x.toFloat() + this.padding + margin,
                pointer.y.toFloat(),
                paint.also {
                    it.color = color
                }
            )
            pointer.y += lineSpace
        }
        pointer.y += offset + this.padding + margin - lineSpace
    }
}