@file:Suppress("UNUSED")

package top.e404.skiko.draw.element

import top.e404.skiko.draw.DrawElement
import top.e404.skiko.draw.LIGHT_BLUE
import top.e404.skiko.draw.Pointer
import top.e404.skiko.draw.WHITE
import org.jetbrains.skia.*

/**
 * 代表一行高亮序号的文本, 为了显示效果, 此组件仅限1行, 超出部分会压缩并留下`...`
 *
 * @property content 正文
 * @property font 字体
 * @property color 字体颜色
 * @property udPadding 上下边距
 * @property left 左侧边距
 * @property offset 偏移调整
 */
class TextWithIndex(
    var content: String,
    var font: Font,
    var color: Int = WHITE,
    var index: String,
    var indexMargin: Float = font.size / 3,
    var indexLength: Int = 2,
    var indexTextColor: Int = WHITE,
    var indexBgColor: Int = LIGHT_BLUE,
    var contentLeft: Int = 40,
    var udPadding: Int = 20,
    var left: Int = 0,
    var offset: Int = 14,
) : DrawElement {
    lateinit var indexLine: TextLine
    var indexWidth = 0F
    var indexHeight = 0F
    lateinit var line: TextLine

    override fun size(minWidth: Int, maxWidth: Int): Pair<Int, Int> {
        indexLine = TextLine.make(index, font)
        indexWidth = TextLine.make("".padStart(indexLength, '0'), font).width + indexMargin * 2
        indexHeight = font.size + (indexMargin) * 2
        var end = content.length
        var width: Float
        do {
            var text = content.substring(0, end--)
            if (text.length != content.length) text = "$text..."
            line = TextLine.make(text, font)
            width = left + line.width + indexWidth + indexMargin
        } while (width > maxWidth)
        return Pair(width.toInt(), font.size.toInt() + (indexMargin.toInt() + udPadding) * 2)
    }

    override fun drawToBoard(canvas: Canvas, pointer: Pointer, paint: Paint, width: Int, imagePadding: Int) {
        // index bg
        pointer.y += udPadding
        canvas.drawRRect(RRect.makeXYWH(
            pointer.x.toFloat() + left,
            pointer.y.toFloat(),
            indexWidth,
            indexHeight,
            font.size / 3
        ), paint.apply {
            color = this@TextWithIndex.indexBgColor
        })
        // index text
        pointer.y += font.size.toInt() - offset
        canvas.drawTextLine(
            indexLine,
            left + pointer.x + indexMargin + (indexWidth - indexMargin * 2 - indexLine.width) / 2,
            pointer.y + indexMargin,
            paint.apply {
                color = this@TextWithIndex.indexTextColor
            }
        )
        // text line
        canvas.drawTextLine(
            line,
            left + pointer.x + indexWidth + contentLeft,
            pointer.y + indexMargin,
            paint.apply {
                color = this@TextWithIndex.color
            }
        )
        pointer.y += udPadding + indexMargin.toInt() * 2 + offset
    }
}