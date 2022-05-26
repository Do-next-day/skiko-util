package top.e404.skiko.handler.face

import org.jetbrains.skia.ColorFilter
import org.jetbrains.skia.ColorMatrix
import org.jetbrains.skia.Paint
import org.jetbrains.skia.TextLine
import top.e404.skiko.Colors
import top.e404.skiko.FontType
import top.e404.skiko.apt.annotation.ImageHandler
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common
import top.e404.skiko.frame.handle
import top.e404.skiko.util.toSurface
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
            .1F, .1F, .1F, 0F, 0F,
            .1F, .1F, .1F, 0F, 0F,
            .5F, .5F, .5F, .3F, 0F,
            0F, 0F, 0F, 1F, 0F,
        )
    )
    private val mp = Paint().apply { colorFilter = matrix }

    override val name = "群青"
    override val regex = Regex("(?i)群青")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.result {
        val left = args["left"]?.let {
            if (it.endsWith("%")) -it.removeSuffix("%").toDouble()
            else it.toDouble()
        }
        val top = args["top"]?.let {
            if (it.endsWith("%")) -it.removeSuffix("%").toDouble()
            else it.toDouble()
        }
        common(args).handle {
            toSurface().withCanvas {
                drawImage(this@handle, 0f, 0f, mp)
                val font1 = FontType.MI_BOLD.getSkiaFont(this@handle.height / 8F)
                val font2 = FontType.MI_BOLD.getSkiaFont(this@handle.height / 10F)
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
                    left == null -> this@handle.width / 5 * 4.0
                    left < 0 -> this@handle.width * -left / 100
                    else -> left.toDouble()
                }.toInt()
                val t = when {
                    top == null -> this@handle.height / 3.0
                    top < 0 -> this@handle.height * -top / 100
                    else -> top.toDouble()
                }.toInt()
                drawTextLine(line1, l - w1, t + h1, paint)
                drawTextLine(line2, l - w2, t + h1 + h2, paint)
                drawTextLine(line3, l - w3, t + h1 + h2 + h3, paint)
            }
        }
    }
}