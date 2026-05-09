package top.e404.skiko.handler.list

import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.Paint
import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common
import top.e404.skiko.frame.handle
import top.e404.skiko.util.newSurface
import top.e404.skiko.util.withCanvas

/**
 * 彩色浮雕效果
 */
@ImageHandler
object EmbossColorHandler : FramesHandler {
    override val name = "彩色浮雕"
    override val regex = Regex("(?i)彩色浮雕|cfd|fdc|EmbossColor|EmbossC")
    private val kernel = floatArrayOf(
        -3f, 0f, 0f,
        0f, 1f, 0f,
        0f, 0f, 3f
    )

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.result {
        common(args).handle {
            val filter = ImageFilter.makeMatrixConvolution(
                kernelW = 3,
                kernelH = 3,
                kernel = kernel,
                gain = 1f,
                bias = 1f,
                offsetX = 0,
                offsetY = 0,
                tileMode = FilterTileMode.CLAMP,
                convolveAlpha = false,
                input = null,
                crop = null
            )
            it.newSurface().withCanvas {
                drawImage(it, 0f, 0f, Paint().apply { imageFilter = filter })
            }
        }
    }
}
