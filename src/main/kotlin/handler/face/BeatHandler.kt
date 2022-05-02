package top.e404.skiko.handler.face

import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import top.e404.skiko.Colors
import top.e404.skiko.apt.annotation.ImageHandler
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common
import top.e404.skiko.frame.handle
import top.e404.skiko.util.getJarImage
import top.e404.skiko.util.subCenter
import top.e404.skiko.util.toSurface
import top.e404.skiko.util.withCanvas

/**
 * 铁咩
 */
@ImageHandler
object BeatHandler : FramesHandler {
    private val bg = getJarImage("statistic/beat.png")
    private val bgRect = Rect.makeWH(bg.width.toFloat(), bg.height.toFloat())
    private val rect = Rect.makeXYWH(5F, 60F, 100F, 100F)
    private val paint = Paint().apply { color = Colors.WHITE.argb }

    override val name = "铁咩"
    override val regex = Regex("(?i)铁咩|tm")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.result {
        common(args).handle {
            bg.toSurface().withCanvas {
                drawRect(bgRect, paint)
                drawImageRect(subCenter(), rect)
                drawImage(bg, 0F, 0F)
            }
        }
    }
}