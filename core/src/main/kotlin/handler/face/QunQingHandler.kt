package top.e404.skiko.handler.face

import org.jetbrains.skia.ColorFilter
import org.jetbrains.skia.ColorMatrix
import org.jetbrains.skia.Paint
import org.jetbrains.skia.TextLine
import top.e404.skiko.util.Colors
import top.e404.skiko.FontType
import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common
import top.e404.skiko.frame.handle
import top.e404.skiko.util.doubleOrPercentage
import top.e404.skiko.util.newSurface
import top.e404.skiko.util.withCanvas

/**
 * 群青
 */
@ImageHandler
object QunQingHandler : FramesHandler {
    private val paint = Paint().apply {
        color = Colors.WHITE.argb
    }
    private val matrix = ColorFilter.makeMatrix(
        ColorMatrix(
            0.35F, 0.35F, 0.35F, 0.0F, 0.0F,
            0.35F, 0.35F, 0.35F, 0.0F, 0.0F,
            0.5F, 0.5F, 0.5F, 0.3F, 0.0F,
            0.0F, 0.0F, 0.0F, 1.0F, 0.0F,
        )
    )
    private val mp = Paint().apply { colorFilter = matrix }

    override val name = "群青"
    override val regex = Regex("(?i)群青")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.result {
        val left = args["left"].doubleOrPercentage(null)
        val top = args["top"].doubleOrPercentage(null)
        common(args).handle {
            it.newSurface().withCanvas {
                drawImage(it, 0f, 0f, mp)
                val font1 = FontType.MI_BOLD.getSkiaFont(it.height / 8F)
                val font2 = FontType.MI_BOLD.getSkiaFont(it.height / 10F)
                val line1 = TextLine.make("群", font1)
                val line2 = TextLine.make("青", font1)
                val line3 = TextLine.make("YOASOBI", font2)
                val h1 = line1.height
                val w1 = line1.width
                val h2 = line2.height
                val w2 = line2.width
                val h3 = line3.height
                val w3 = line3.width
                val l = when {
                    left == null -> it.width / 5 * 4.0
                    left < 0 -> it.width * -left / 100
                    else -> left.toDouble()
                }.toInt()
                val t = when {
                    top == null -> it.height / 3.0
                    top < 0 -> it.height * -top / 100
                    else -> top.toDouble()
                }.toInt()
                drawTextLine(line1, l - w1, t + h1, paint)
                drawTextLine(line2, l - w2, t + h1 + h2, paint)
                drawTextLine(line3, l - w3, t + h1 + h2 + h3, paint)
            }
        }
    }
}
