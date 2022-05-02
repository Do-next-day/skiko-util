package top.e404.skiko.handler.list

import org.jetbrains.skia.Matrix33
import top.e404.skiko.apt.annotation.ImageHandler
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common
import top.e404.skiko.frame.withCanvas

/**
 * 垂直翻转 `b -> d`
 */
@ImageHandler
object FlipVerticalHandler : FramesHandler {
    override val name = "垂直翻转"
    override val regex = Regex("(?i)垂直翻转|左右翻转|czfz|sxfz|szfz")
    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.result {
        common(args).withCanvas { image ->
            setMatrix(
                Matrix33(
                    -1F, 0F, image.width.toFloat(),
                    0F, 1F, 0F,
                    0F, 0F, 1F
                )
            )
            drawImage(image, 0F, 0F)
        }
    }
}