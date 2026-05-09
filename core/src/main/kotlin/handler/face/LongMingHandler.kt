package top.e404.skiko.handler.face

import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import top.e404.skiko.util.Colors
import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common
import top.e404.skiko.frame.handle
import top.e404.skiko.util.*

/**
 * 龙鸣
 */
@ImageHandler
object LongMingHandler : FramesHandler {
    private val bg = getJarImage(this::class.java, "statistic/lm.png")
    private val bgRect = Rect.makeWH(bg.width.toFloat(), bg.height.toFloat())
    private val paint = Paint().apply {
        color = Colors.WHITE.argb
    }

    override val name = "龙鸣"
    override val regex = Regex("龙鸣|(?i)lm")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.result {
        common(args).handle {
            bg.newSurface().withCanvas {
                drawRect(bgRect, paint)
                val face = it.subCenter()
                drawImageRectNearest(
                    face,
                    Rect.makeWH(face.width.toFloat(), face.height.toFloat()),
                    Rect.makeXYWH(228F, 126F, 234F, 234F)
                )
                drawImage(bg, 0F, 0F)
            }
        }
    }
}
