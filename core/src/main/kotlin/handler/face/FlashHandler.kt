package top.e404.skiko.handler.face

import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import top.e404.skiko.util.Colors
import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common
import top.e404.skiko.frame.handle
import top.e404.skiko.util.getJarImage
import top.e404.skiko.util.resize
import top.e404.skiko.util.withCanvas

@ImageHandler
object FlashHandler : FramesHandler {
    private val cover = getJarImage(this::class.java, "statistic/flash.png")
    private const val w = 600
    private const val h = 450
    private val imgRect = Rect.makeWH(w.toFloat(), h.toFloat())
    private val paint = Paint().apply {
        color = Colors.WHITE.argb
    }
    private val alphaPaint = Paint().apply {
        alpha = 192
    }

    override val name = "flash"
    override val regex = Regex("闪照|(?i)flash")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.result {
        common(args).handle {
            Surface.makeRasterN32Premul(w, h).withCanvas {
                drawRect(imgRect, paint)
                drawImage(it.resize(8, 7, true).resize(w, h, true), 0f, 0f)
                drawImage(cover, 0f, 0f, alphaPaint)
            }
        }
    }
}
