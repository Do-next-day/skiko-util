package top.e404.skiko.handler.face

import org.jetbrains.skia.*
import top.e404.skiko.util.Colors
import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common
import top.e404.skiko.frame.handle
import top.e404.skiko.util.*

@ImageHandler
object KnockHandler : FramesHandler {
    private val bg = getJarImage(this::class.java, "statistic/knock.png")
    private val bgRect = Rect.makeWH(bg.width.toFloat(), bg.height.toFloat())
    private val paint = Paint().apply {
        color = Colors.WHITE.argb
    }

    override val name = "敲"
    override val regex = Regex("(?i)敲|qiao|knock")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.result {
        common(args).handle {
            bg.newSurface().withCanvas {
                drawRect(bgRect, paint)
                drawImageRectNearest(
                    image = it.subCenter(),
                    dst = Rect.makeXYWH(20F, 114F, 100F, 100F)
                )
                drawImage(bg, 0F, 0F)
            }
        }
    }
}
