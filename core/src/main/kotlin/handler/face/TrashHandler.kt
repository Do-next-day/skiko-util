package top.e404.skiko.handler.face

import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import top.e404.skiko.util.Colors
import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common
import top.e404.skiko.frame.handle
import top.e404.skiko.util.drawImageRectNearest
import top.e404.skiko.util.getJarImage
import top.e404.skiko.util.round
import top.e404.skiko.util.withCanvas

/**
 * 垃圾
 */
@ImageHandler
object TrashHandler : FramesHandler {
    private const val w = 116
    private const val h = 118
    private val bg by lazy { getJarImage(this::class.java, "statistic/trash.png") }
    private val rect = Rect.makeXYWH(41F, 30F, 68F, 68F)

    override val name = "Trash"
    override val regex = Regex("(?i)trash")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.handle { it.round() }.common(args).result {
        handle {
            val src = Rect.makeWH(it.width.toFloat(), it.height.toFloat())
            Surface.makeRasterN32Premul(w, h).withCanvas {
                clear(Colors.WHITE.argb)
                drawImageRectNearest(it, src, rect)
                drawImage(bg, 0F, 0F)
            }
        }
    }
}
