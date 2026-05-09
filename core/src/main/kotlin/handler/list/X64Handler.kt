package top.e404.skiko.handler.list

import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common
import top.e404.skiko.frame.handle
import top.e404.skiko.util.getJarImage
import top.e404.skiko.util.newSurface
import top.e404.skiko.util.resize
import top.e404.skiko.util.withCanvas
import kotlin.math.min

/**
 * x64
 */
@ImageHandler
object X64Handler : FramesHandler {
    private val x64 by lazy { getJarImage(this::class.java, "statistic/64x.png") }

    override val name = "x64"
    override val regex = Regex("(?i)x64|64x")
    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.result {
        val first = first()
        val size = min(first.image.width, first.image.height)
        val resize = x64.resize(size / 2, size / 3, true)
        common(args).handle {
            it.newSurface().withCanvas {
                drawImage(it, 0f, 0f)
                drawImage(
                    resize,
                    it.width.toFloat() - resize.width,
                    it.height.toFloat() - resize.height
                )
            }
        }
    }
}
