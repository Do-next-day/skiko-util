package top.e404.skiko.handler.face

import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import top.e404.skiko.Colors
import top.e404.skiko.apt.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.util.getJarImage
import top.e404.skiko.util.round
import top.e404.skiko.util.toSurface
import top.e404.skiko.util.withCanvas

/**
 * 打针
 */
@ImageHandler
object InjectionHandler : FramesHandler {
    private val bg = getJarImage("statistic/injection.png")
    private val bgRect = Rect.makeWH(bg.width.toFloat(), bg.height.toFloat())
    private val paint = Paint().apply {
        color = Colors.WHITE.argb
    }

    override val name = "打针"
    override val regex = Regex("(?i)打针|dz")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.result {
        common(args).handle {
            bg.toSurface().withCanvas {
                val round = round()
                drawRect(bgRect, paint)
                val rr = Rect.makeWH(round.width.toFloat(), round.height.toFloat())
                drawImageRect(round, rr, Rect.makeXYWH(150F, 90F, 100F, 105F))
                drawImageRect(round, rr, Rect.makeXYWH(85F, 85F, 110F, 110F))
                drawImage(bg, 0F, 0F)
            }
        }
    }
}