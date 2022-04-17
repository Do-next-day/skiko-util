package top.e404.skiko.handler.face

import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import top.e404.skiko.apt.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.util.getJarImage
import top.e404.skiko.util.rotateKeepSize
import top.e404.skiko.util.round
import top.e404.skiko.util.withCanvas

@ImageHandler
object ThrowHandler : FramesHandler {
    private const val size = 448
    private val bg = getJarImage("statistic/throw.png")

    override val name = "丢"
    override val regex = Regex("(?i)丢|diu|throw")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.result {
        common(args).handle {
            Surface.makeRasterN32Premul(
                this@ThrowHandler.size,
                this@ThrowHandler.size
            ).withCanvas {
                drawImage(bg, 0F, 0F)
                drawImageRect(
                    round().rotateKeepSize(270F),
                    Rect.makeXYWH(10F, 175F, 150F, 150F)
                )
            }
        }
    }
}