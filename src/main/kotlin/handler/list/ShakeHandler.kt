package top.e404.skiko.handler.list

import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import top.e404.bot.botutil.pmapIndexed
import top.e404.skiko.apt.annotation.ImageHandler
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common
import top.e404.skiko.util.withCanvas

@ImageHandler
object ShakeHandler : FramesHandler {
    override val name = "抖动"
    override val regex = Regex("(?i)抖动|shake")
    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ): HandleResult {
        val v = args["size"]?.toIntOrNull() ?: 20
        return frames.result {
            common(args).run {
                if (size == 1) (1..10).map { frames[0].clone() }
                else frames
            }.pmapIndexed { index ->
                handle {
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