package top.e404.skiko.handler.list

import org.jetbrains.skia.Paint
import org.jetbrains.skia.Surface
import org.jetbrains.skia.TextLine
import top.e404.skiko.FontType
import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.dot.binary
import top.e404.skiko.dot.generator
import top.e404.skiko.dot.gray
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common
import top.e404.skiko.frame.handle
import top.e404.skiko.util.*

@ImageHandler
object DotMatrixHandler : FramesHandler {
    private val font = FontType.HEI.getSkiaFont(20F)
    private val fullHeight = font.height()

    override val name = "点阵字符画"
    override val regex = Regex("(?i)点阵字符画|dot")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.result {
        val color = args["color"]?.asColor() ?: Colors.WHITE.argb
        val bg = args["bg"]?.asColor() ?: Colors.BG.argb
        val paint = Paint().also { it.color = color }
        common(args).handle { src ->
            val bitImage = binary(gray(src))
            val text = bitImage.generator(
                args["ud"]?.toIntOrNull() ?: 2,
                args["lr"]?.toIntOrNull() ?: 2,
            )
            val lines = text.lines().map {
                TextLine.make(it, font)
            }

            Surface.makeRasterN32Premul(lines[0].width.toInt(), (fullHeight * lines.size).toInt()).fill(bg).withCanvas {
                lines.forEachIndexed { index, line ->
                    drawTextLine(line, 0F, index * fullHeight - font.metrics.ascent, paint)
                }
            }
        }
    }
}
