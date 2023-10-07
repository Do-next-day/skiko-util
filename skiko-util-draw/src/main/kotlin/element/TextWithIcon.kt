@file:Suppress("UNUSED")

package top.e404.skiko.draw.element

import org.jetbrains.skia.*
import top.e404.skiko.util.Colors
import top.e404.skiko.draw.DrawElement
import top.e404.skiko.draw.Pointer

/**
 * 代表一行左侧有标记的文本, 为了显示效果, 此组件仅限1行, 超出部分会压缩并留下`...`
 *
 * @property content 正文
 * @property font 字体
 * @property color 字体颜色
 * @property iconColor 标记颜色
 * @property udPadding 上下边距
 * @property left 左侧边距
 */
class TextWithIcon(
    var content: String,
    var font: Font,
    var color: Int = Colors.WHITE.argb,
    var iconColor: Int = Colors.LIGHT_BLUE.argb,
    var udPadding: Int = 20,
    var left: Int = 0,
) : DrawElement {
    lateinit var line: TextLine

    override fun size(minWidth: Int, maxWidth: Int): Pair<Float, Float> {
        var end = content.length
        var width: Float
        do {
            var text = content.substring(0, end--)
            if (text.length != content.length) text = "$text..."
            line = TextLine.make(text, font)
            width = line.width + font.size
        } while (width > maxWidth)
        return width to line.descent - line.ascent + 2 * udPadding
    }

    override fun drawToBoard(
        canvas: Canvas,
        pointer: Pointer,
        paint: Paint,
        width: Int,
        imagePadding: Int,
        debug: Boolean
    ) {
        // icon
        pointer.y += udPadding
        canvas.drawRRect(
            r = RRect.makeXYWH(
                l = pointer.x,
                t = pointer.y + line.descent / 2,
                w = font.size / 2,
                h = font.size,
                radius = font.size / 4
            ),
            paint = paint.apply { color = iconColor })
        // text line
        pointer.y -= line.ascent
        canvas.drawTextLine(
            line = line,
            x = pointer.x + font.size,
            y = pointer.y,
            paint = paint.also { it.color = color }
        )
        pointer.y += line.descent + udPadding
    }
}
