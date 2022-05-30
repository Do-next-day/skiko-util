package top.e404.skiko.handler.list

import org.jetbrains.skia.*
import top.e404.skiko.apt.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.util.*
import kotlin.math.min

/**
 * 方格化1
 */
@ImageHandler
object PaneHandler : FramesHandler {
    override val name = "Pane"
    override val regex = Regex("(?i)pane")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ): HandleResult {
        val rate = args["text"]?.intOrPercentage() ?: -10
        return frames.result {
            common(args).handle {
                val src = toBitmap()
                val r = if (rate > 0) rate else rate * min(src.width, src.height) / -100
                val dst = Bitmap().apply {
                    val cInfo = src.imageInfo.colorInfo.apply { setAlphaType(ColorAlphaType.PREMUL) }
                    allocPixels(ImageInfo(cInfo, src.width - 2 * r, src.height - 2 * r))
                }
                for (x in 0 until dst.width) for (y in 0 until dst.height) {
                    val vx = r + x + x % r
                    val vy = r + y + y % r
                    dst.erase(src.getColor(vx, vy), IRect.makeXYWH(x, y, 1, 1))
                }
                dst.toImage()
            }
        }
    }
}