package top.e404.skiko.handler.list

import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.Paint
import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result

@ImageHandler
object BlurHandler : FramesHandler {
    override val name = "高斯模糊"
    override val regex = Regex("(高斯)?模糊|(?i)blur|(gs)?mh")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ): HandleResult {
        var size = args["text"]?.toFloatOrNull() ?: 10F
        if (size < 0) size = -size
        else if (size == 0F) size = 10F
        return frames.result {
            common(args).withCanvas { image ->
                drawImage(image, 0F, 0F, Paint().apply {
                    imageFilter = ImageFilter.makeBlur(size, size, FilterTileMode.REPEAT)
                })
            }
        }
    }
}

