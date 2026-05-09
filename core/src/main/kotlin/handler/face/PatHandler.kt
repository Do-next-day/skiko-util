package top.e404.skiko.handler.face

import org.jetbrains.skia.Rect
import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common
import top.e404.skiko.frame.handle
import top.e404.skiko.util.*

/**
 * 拍
 */
@ImageHandler
object PatHandler : FramesHandler {
    private val bg = getJarImage(this::class.java, "statistic/pat.png")

    override val name = "拍"
    override val regex = Regex("(?i)拍|pai")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.result {
        common(args).handle {
            val face = it.round()
            bg.newSurface().withCanvas {
                drawImage(bg, 0F, 0F)
                drawImageRectNearest(
                    face,
                    Rect.makeWH(face.width.toFloat(), face.height.toFloat()),
                    Rect.makeXYWH(230F, 270F, 150F, 150F)
                )
            }
        }
    }
}
