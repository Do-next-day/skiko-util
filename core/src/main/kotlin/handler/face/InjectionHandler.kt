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
 * 打针
 */
@ImageHandler
object InjectionHandler : FramesHandler {
    private val bg = getJarImage(this::class.java, "statistic/injection.png")
    private val bgRect = Rect.makeWH(bg.width.toFloat(), bg.height.toFloat())
    private val paint = Paint().apply {
        color = Colors.WHITE.argb
    }

    override val name = "打针"
    override val regex = Regex("打针|(?i)dz")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.result {
        common(args).handle {
            bg.newSurface().withCanvas {
                val round = it.round()
                drawRect(bgRect, paint)
                val rr = Rect.makeWH(round.width.toFloat(), round.height.toFloat())
                drawImageRectNearest(round, rr, Rect.makeXYWH(150F, 90F, 100F, 105F))
                drawImageRectNearest(round, rr, Rect.makeXYWH(85F, 85F, 110F, 110F))
                drawImage(bg, 0F, 0F)
            }
        }
    }
}
