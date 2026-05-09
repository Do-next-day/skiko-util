package top.e404.skiko.generator.list

import org.jetbrains.skia.Paint
import org.jetbrains.skia.Surface
import org.jetbrains.skia.TextLine
import top.e404.skiko.BdfType
import top.e404.skiko.FontType
import top.e404.skiko.dot.generator
import top.e404.skiko.dot.toBitMatrix
import top.e404.skiko.frame.Frame
import top.e404.skiko.generator.ImageGenerator
import top.e404.skiko.util.*

object DotMatrixCharImageGenerator : ImageGenerator {
    private val font = FontType.HEI.getSkiaFont(20F)
    private val fullHeight = font.height()
    override suspend fun generate(args: MutableMap<String, String>): MutableList<Frame> {
        val text = args["text"]!!
        val color = args["color"]?.asColor() ?: Colors.WHITE.argb
        val bg = args["bg"]?.asColor() ?: Colors.BG.argb
        val paint = Paint().also { it.color = color }

        val matrix = text.toBitMatrix(BdfType.UNI_FONT.font)
        val generator = matrix.generator(
            args["ud"]?.toIntOrNull() ?: 0,
            args["lr"]?.toIntOrNull() ?: 0
        )
        val lines = generator.lines().map { TextLine.make(it, font) }

        return mutableListOf(
            Frame(0, Surface.makeRasterN32Premul(lines[0].width.toInt(), (fullHeight * lines.size).toInt()).fill(bg).withCanvas {
                lines.forEachIndexed { index, line ->
                    drawTextLine(line, 0F, index * fullHeight - font.metrics.ascent, paint)
                }
            })
        )
    }
}
