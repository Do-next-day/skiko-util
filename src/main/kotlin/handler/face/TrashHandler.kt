package top.e404.skiko.handler.face

import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import top.e404.skiko.Colors
import top.e404.skiko.apt.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
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
    private val bg by lazy { getJarImage("statistic/trash.png") }
    private val rect = Rect.makeXYWH(41F, 30F, 68F, 68F)

    override val name = "Trash"
    override val regex = Regex("(?i)trash")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.handle { round() }.common(args).result {
        handleIndexed { index ->
            Surface.makeRasterN32Premul(w, h).withCanvas {
                clear(Colors.WHITE.argb)
                drawImageRect(this@handleIndexed, rect)
                drawImage(bg, 0F, 0F)
            }
        }
    }
}