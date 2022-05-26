package top.e404.skiko.handler.list

import top.e404.skiko.apt.annotation.ImageHandler
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common
import top.e404.skiko.frame.handle
import top.e404.skiko.util.getJarImage
import top.e404.skiko.util.resize
import top.e404.skiko.util.toSurface
import top.e404.skiko.util.withCanvas
import kotlin.math.min

/**
 * x64
 */
@ImageHandler
object X64Handler : FramesHandler {
    private val x64 by lazy { getJarImage("statistic/64x.png") }

    override val name = "x64"
    override val regex = Regex("(?i)x64|64x")
    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.result {
        val first = first()
        val size = min(first.image.width, first.image.height)
        val resize = x64.resize(size / 2, size / 3, false)
        common(args).handle {
            toSurface().withCanvas {
                drawImage(this@handle, 0f, 0f)
                drawImage(
                    resize,
                    this@handle.width.toFloat() - resize.width,
                    this@handle.height.toFloat() - resize.height
                )
            }
        }
    }
}