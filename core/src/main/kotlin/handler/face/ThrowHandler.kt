package top.e404.skiko.handler.face

import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common
import top.e404.skiko.frame.handle
import top.e404.skiko.util.*

@ImageHandler
object ThrowHandler : FramesHandler {
    private const val size = 448
    private val bg = getJarImage(this::class.java, "statistic/throw.png")

    override val name = "丢"
    override val regex = Regex("(?i)丢|diu|throw")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.result {
        common(args).handle {
            val img = it.round().rotateKeepSize(270F)
            val src = Rect.makeWH(img.width.toFloat(), img.height.toFloat())
            Surface.makeRasterN32Premul(
                this@ThrowHandler.size,
                this@ThrowHandler.size
            ).withCanvas {
                drawImage(bg, 0F, 0F)
                drawImageRectNearest(
                    image = img,
                    src = src,
                    dst = Rect.makeXYWH(10F, 175F, 150F, 150F)
                )
            }
        }
    }
}
