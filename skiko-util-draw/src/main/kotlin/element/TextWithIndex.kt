@file:Suppress("UNUSED")

package top.e404.skiko.draw.element

import org.jetbrains.skia.*
import top.e404.skiko.util.Colors
import top.e404.skiko.draw.DrawElement
import top.e404.skiko.draw.Pointer

/**
 * 代表一行高亮序号的文本, 为了显示效果, 此组件仅限1行, 超出部分会压缩并留下`...`
 *
 * @property content 正文
 * @property font 字体
 * @property color 字体颜色
 * @property index 序号
 * @property indexMargin 序号边距
 * @property indexLength 序号长度
 * @property indexTextColor 序号字体颜色
 * @property indexBgColor 序号背景颜色
 * @property contentLeft 左边距
 * @property udPadding 上下边距
 * @property left 左侧边距
 */
class TextWithIndex(
    var content: String,
    var font: Font,
    var color: Int = Colors.WHITE.argb,
    var index: String,
    var indexMargin: Float = font.size / 4,
    var indexLength: Int = 2,
    var indexTextColor: Int = Colors.WHITE.argb,
    var indexBgColor: Int = Colors.LIGHT_BLUE.argb,
    var contentLeft: Int = 40,
    var udPadding: Int = 20,
    var left: Int = 0,
) : DrawElement {
    lateinit var indexLine: TextLine
    var indexWidth = 0F
    var indexHeight = 0F
    lateinit var line: TextLine

    override fun size(minWidth: Int, maxWidth: Int): Pair<Float, Float> {
        indexLine = TextLine.make(index, font)
        indexWidth = TextLine.make("".padStart(indexLength, '0'), font).width + indexMargin * 2
        indexHeight = font.size + (indexMargin) * 2
        var end = content.length
        var width: Float
        do {
            var text = content.substring(0, end--)
            if (text.length != content.length) text = "$text..."
            line = TextLine.make(text, font)
            width = left + line.width + indexWidth + indexMargin * 2
        } while (width > maxWidth)
        return width to line.descent - line.ascent + (indexMargin + udPadding) * 2
    }

    override fun drawToBoard(
        canvas: Canvas,
        pointer: Pointer,
        paint: Paint,
        width: Int,
        imagePadding: Int,
        debug: Boolean
    ) {
        // index bg
        pointer.y += udPadding
        canvas.drawRRect(
            r = RRect.makeXYWH(
                l = pointer.x + left + indexMargin,
                t = pointer.y + line.descent / 2,
                w = indexWidth,
                h = indexHeight,
                radius = font.size / 3
            ),
            paint = paint.apply { color = indexBgColor })
        // index text
        pointer.y -= line.ascent
        canvas.drawTextLine(
            line = indexLine,
            x = left + pointer.x + indexMargin + (indexWidth - indexMargin * 2 - indexLine.width) / 2,
            y = pointer.y + indexMargin,
            paint = paint.apply { color = indexTextColor }
        )
        // text line
        canvas.drawTextLine(
            line = line,
            x = left + pointer.x + indexWidth + contentLeft,
            y = pointer.y + indexMargin,
            paint = paint.also { it.color = color }
        )
        pointer.y += line.descent + udPadding + indexMargin * 2
    }
}
