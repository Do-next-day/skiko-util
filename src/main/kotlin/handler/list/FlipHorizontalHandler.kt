package top.e404.skiko.handler.list

import org.jetbrains.skia.Matrix33
import top.e404.skiko.apt.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result

/**
 * 水平翻转 `b -> p`
 */
@ImageHandler
object FlipHorizontalHandler : FramesHandler {
    override val name = "水平翻转"
    override val regex = Regex("(?i)(水平|上下)翻转|spfz|sxfz")
    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.result {
        common(args).withCanvas { image ->
            setMatrix(Matrix33(
                1F, 0F, 0F,
                0F, -1F, image.height.toFloat(),
                0F, 0F, 1F
            ))
            drawImage(image, 0F, 0F)
        }
    }
}