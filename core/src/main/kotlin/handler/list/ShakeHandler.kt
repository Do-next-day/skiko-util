package top.e404.skiko.handler.list

import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.util.pmapIndexed
import top.e404.skiko.util.withCanvas

@ImageHandler
object ShakeHandler : FramesHandler {
    override val name = "ShakeImage"
    override val regex = Regex("(?i)shakeImage|shakeImg|si")
    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ): HandleResult {
        val v = args["size"]?.toIntOrNull()?.coerceIn(1..200) ?: 20
        val count = args["count"]?.toIntOrNull()?.coerceIn(1..50) ?: 10
        return frames.replenish(count, Frame::limitAsGif).result {
            common(args).pmapIndexed { index ->
                handleImage {
                    val w = image.width.toFloat()
                    val h = image.height.toFloat()
                    val rect = when (index % 4) {
                        0 -> Rect.makeXYWH(0F, 0F, w, h)
                        1 -> Rect.makeXYWH(v.toFloat(), 0F, w, h)
                        2 -> Rect.makeXYWH(0F, v.toFloat(), w, h)
                        else -> Rect.makeXYWH(v.toFloat(), v.toFloat(), w, h)
                    }
                    Surface.makeRasterN32Premul(image.width + v, image.height + v).withCanvas {
                        drawImageRect(image, rect)
                    }
                }
            }
        }
    }
}
