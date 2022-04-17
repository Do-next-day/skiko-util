package top.e404.skiko.handler.list

import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.ImageFilter
import org.jetbrains.skia.Paint
import top.e404.skiko.apt.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result

@ImageHandler
object BlurHandler : FramesHandler {
    private val fail = HandleResult.fail("size应为大于0的浮点数")

    override val name = "高斯模糊"
    override val regex = Regex("(?i)(高斯)?模糊|blur|mh")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ): HandleResult {
        val size = args["size"]?.toFloatOrNull() ?: return fail
        if (size <= 0) return fail
        return frames.result {
            common(args).withCanvas { image ->
                drawImage(image, 0F, 0F, Paint().apply {
                    imageFilter = ImageFilter.makeBlur(size, size, FilterTileMode.REPEAT)
                })
            }
        }
    }
}

