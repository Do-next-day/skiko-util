@file:Suppress("UNUSED")

package top.e404.skiko.draw.element

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Font
import org.jetbrains.skia.Paint
import org.jetbrains.skia.TextLine
import top.e404.skiko.util.Colors
import top.e404.skiko.draw.DrawElement
import top.e404.skiko.draw.Pointer
import top.e404.skiko.draw.splitByWidth

/**
 * 代表一个文本列表, 计算间距时不计算与下方块之间的间距
 *
 * @property contents 文本列表
 * @property font 字体
 * @property color 字体颜色
 * @property udPadding 行间距, 上一个元素到首行以及末行到下一个元素的间距为1/2行间距
 * @property left 左侧边距
 * @property index 若为null则使用数字序号(有序列表), 否则作为无序列表的列表项开头
 */
open class TextList(
    var contents: List<String>,
    var font: Font,
    var color: Int = Colors.WHITE.argb,
    var udPadding: Int = 10,
    var left: Int = 0,
    var index: String? = null,
) : DrawElement {
    private var lines = listOf<TextListLine>()

    override fun size(minWidth: Int, maxWidth: Int): Pair<Float, Float> {
        // 计算序号字符串最大长度
        val length = (contents.size - 1).toString().length
        val indexWidth = TextLine.make(if (index == null) "${"0".repeat(length)}. " else index, font).width.toInt()
        fun getIndex(i: Int) = index ?: "${(i + 1).toString().padStart(length, ' ')}. "
        lines = contents.withIndex().map { (index, text) ->
            TextListLine(getIndex(index), indexWidth, text, font, color, udPadding, left)
        }
        val sizes = lines.map { it.size(maxWidth) }
        val width = sizes.map { it.first }.maxOf { it }
        val height = sizes.map { it.second }.sum() + sizes.size * udPadding
        return width to height
    }

    override fun drawToBoard(
        canvas: Canvas,
        pointer: Pointer,
        paint: Paint,
        width: Int,
        imagePadding: Int,
        debug: Boolean
    ) {
        pointer.y += udPadding / 2
        for ((index, line) in lines.withIndex()) {
            if (index != 0) pointer.y += udPadding
            line.drawToBoard(
                canvas = canvas,
                pointer = pointer,
                paint = paint
            )
        }
        pointer.y += udPadding / 2
    }

    /**
     * 列表中的一项
     *
     * @property index 序号
     * @property indexWidth 序号的宽度
     * @property content 内容
     * @property font 字体
     * @property color 字体颜色
     * @property udPadding 与其他元素的上下间距
     * @property left 左侧边距
     */
    class TextListLine(
        var index: String,
        var indexWidth: Int,
        var content: String,
        var font: Font,
        var color: Int,
        var udPadding: Int,
        var left: Int,
    ) {
        lateinit var lines: MutableList<TextLine>
        fun size(maxWidth: Int): Pair<Float, Float> {
            val pair = content.splitByWidth(maxWidth - left - indexWidth, font, left)
            lines = pair.first
            return pair.second + left + indexWidth to lines.size * (font.metrics.let { it.descent - it.ascent } + udPadding) - udPadding
        }

        fun drawToBoard(
            canvas: Canvas,
            pointer: Pointer,
            paint: Paint
        ) {
            pointer.y -= font.metrics.ascent
            // 序号
            val indexLine = TextLine.make(index, font)
            var x = pointer.x + left + indexWidth - indexLine.width
            canvas.drawTextLine(
                line = indexLine,
                x = x,
                y = pointer.y,
                paint = paint.also { it.color = color }
            )
            // 内容
            x = pointer.x + left + indexWidth
            for ((index, line) in lines.withIndex()) {
                if (index != 0) pointer.y += udPadding - font.metrics.ascent
                canvas.drawTextLine(
                    line = line,
                    x = x,
                    y = pointer.y,
                    paint = paint.also { it.color = color }
                )
                pointer.y += font.metrics.descent
            }
        }
    }
}
