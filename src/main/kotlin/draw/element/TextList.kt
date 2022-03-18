@file:Suppress("UNUSED")

package top.e404.skiko.draw.element

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Font
import org.jetbrains.skia.Paint
import org.jetbrains.skia.TextLine
import top.e404.skiko.Colors
import top.e404.skiko.draw.DrawElement
import top.e404.skiko.draw.Pointer
import top.e404.skiko.draw.splitByWidth

/**
 * 代表一个文本列表, 计算间距时不计算与下方块之间的间距
 *
 * @property contents 文本列表
 * @property font 字体
 * @property color 字体颜色
 * @property udPadding 与上下元素的间隔
 * @property lineSpacing 行间距
 * @property left 左侧边距
 * @property index 若为null则使用数字序号(有序列表), 否则作为无序列表的列表项开头
 * @property offset 偏移调整
 */
open class TextList(
    var contents: List<String>,
    var font: Font,
    var color: Int = Colors.WHITE.value,
    var udPadding: Int = 10,
    var lineSpacing: Int = 10,
    var left: Int = 0,
    var index: String? = null,
    var offset: Int = 14,
) : DrawElement {
    private var lines = ArrayList<TextListLine>()
    var width = 0
    var height = 0
    var indexWidth = 0

    override fun size(minWidth: Int, maxWidth: Int): Pair<Int, Int> {
        val length = (contents.size - 1).toString().length
        indexWidth = TextLine.make(
            if (index == null) "${"0".repeat(length)}. "
            else index, font
        ).width.toInt()
        fun getIndex(i: Int) = index ?: "${(i + 1).toString().padStart(length, ' ')}. "
        lines = ArrayList(contents.withIndex().map { (index, text) ->
            TextListLine(getIndex(index), indexWidth, text, font, color, font.size, lineSpacing, left)
        })
        val sizes = lines.map { it.size(minWidth, maxWidth) }
        width = sizes.map { it.first }.maxOf { it }
        height = sizes.map { it.second }.sumOf { it }
        return Pair(width, height + 2 * udPadding)
    }

    override fun drawToBoard(
        canvas: Canvas,
        pointer: Pointer,
        paint: Paint,
        width: Int,
        imagePadding: Int,
    ) {
        pointer.y += udPadding - offset
        for (line in lines) {
            line.drawToBoard(canvas, pointer, paint, width, imagePadding)
        }
        pointer.y += udPadding + offset
    }

    class TextListLine(
        var index: String,
        var indexWidth: Int,
        var content: String,
        var font: Font,
        var color: Int,
        var fontSize: Float,
        var udPadding: Int,
        var left: Int,
    ) : DrawElement {
        lateinit var lines: ArrayList<TextLine>
        var width = 0
        override fun size(minWidth: Int, maxWidth: Int): Pair<Int, Int> {
            val pair = content.splitByWidth(maxWidth - left - indexWidth, font, left)
            lines = pair.first
            width = pair.second
            return Pair(width + left + indexWidth, lines.size * (fontSize + udPadding).toInt() + udPadding)
        }

        override fun drawToBoard(
            canvas: Canvas,
            pointer: Pointer,
            paint: Paint,
            width: Int,
            imagePadding: Int,
        ) {
            pointer.y += udPadding / 2 + fontSize.toInt()
            val indexLine = TextLine.make(index, font)
            var x = pointer.x + left + indexWidth - indexLine.width
            canvas.drawTextLine(
                indexLine,
                x,
                pointer.y.toFloat(),
                paint.apply {
                    color = this@TextListLine.color
                }
            )
            x = pointer.x + left + indexWidth.toFloat()
            for (line in lines) {
                canvas.drawTextLine(
                    line,
                    x,
                    pointer.y.toFloat(),
                    paint.apply {
                        color = this@TextListLine.color
                    }
                )
                pointer.y += fontSize.toInt() + udPadding
            }
            pointer.y += udPadding / 2 - fontSize.toInt()
        }
    }
}