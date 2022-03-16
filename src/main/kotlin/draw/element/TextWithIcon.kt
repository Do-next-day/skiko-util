@file:Suppress("UNUSED")

package top.e404.skiko.draw.element

import top.e404.skiko.draw.DrawElement
import top.e404.skiko.draw.LIGHT_BLUE
import top.e404.skiko.draw.Pointer
import top.e404.skiko.draw.WHITE
import org.jetbrains.skia.*

/**
 * 代表一行左侧有标记的文本, 为了显示效果, 此组件仅限1行, 超出部分会压缩并留下`...`
 *
 * @property content 正文
 * @property font 字体
 * @property color 字体颜色
 * @property udPadding 上下边距
 * @property left 左侧边距
 * @property offset 偏移调整
 */
class TextWithIcon(
    var content: String,
    var font: Font,
    var color: Int = WHITE,
    var iconColor: Int = LIGHT_BLUE,
    var udPadding: Int = 20,
    var left: Int = 0,
    var offset: Int = 14,
) : DrawElement {
    lateinit var line: TextLine

    override fun size(minWidth: Int, maxWidth: Int): Pair<Int, Int> {
        var end = content.length
        var width: Float
        do {
            var text = content.substring(0, end--)
            if (text.length != content.length) text = "$text..."
            line = TextLine.make(text, font)
            width = line.width + font.size
        } while (width > maxWidth)
        return Pair(width.toInt(), font.size.toInt() + 2 * udPadding)
    }

    override fun drawToBoard(canvas: Canvas, pointer: Pointer, paint: Paint, width: Int, imagePadding: Int) {
        // icon
        pointer.y += udPadding
        canvas.drawRRect(RRect.makeXYWH(
            pointer.x.toFloat(),
            pointer.y.toFloat(),
            font.size / 2,
            font.size,
            10F
        ), paint.apply {
            color = this@TextWithIcon.iconColor
        })
        // text line
        pointer.y += font.size.toInt() - offset
        canvas.drawTextLine(
            line,
            pointer.x + font.size,
            pointer.y.toFloat(),
            paint.apply {
                color = this@TextWithIcon.color
            }
        )
        pointer.y += udPadding + offset
    }
}