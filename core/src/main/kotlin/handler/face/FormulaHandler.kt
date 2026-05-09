package top.e404.skiko.handler.face

import org.jetbrains.skia.Rect
import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.util.*

/**
 * 铁咩
 */
@ImageHandler
object FormulaHandler : FramesHandler {
    private const val count = 67
    private val bgList by lazy {
        (0..count).map { getJarImage(this::class.java, "statistic/formula/$it.png") }
    }

    override val name = "formula"
    override val regex = Regex("(?i)formula|公式")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.result {
        val r = args.containsKey("r")
        val single = size == 1
        val list = common(args).replenish(count + 1) { limitAsGif(300F) }.handleIndexed { index, image ->
            val cover = bgList[if (r) 67 - index % 68 else index % 68]
            image.newSurface().withCanvas {
                drawImage(image, 0F, 0F)
                // 图片宽高比大于16:9
                if (image.width.toFloat() / image.height > 16F / 9) {
                    val h = image.width * cover.height / cover.width
                    val w = image.width
                    drawImageRect(
                        image = cover,
                        dst = Rect.makeXYWH(
                            l = (image.width - w) / 2F,
                            t = (image.height - h) / 2F,
                            w = w.toFloat(),
                            h = h.toFloat()
                        ),
                    )
                    return@withCanvas
                }
                val w = image.height * cover.width / cover.height
                val h = image.height
                // 图片宽高比小于16:9
                drawImageRect(
                    image = cover,
                    dst = Rect.makeXYWH(
                        l = (image.width - w) / 2F,
                        t = (image.height - h) / 2F,
                        w = w.toFloat(),
                        h = h.toFloat()
                    ),
                )
            }
        }
        if (single) list.forEach { it.duration = 60 }
        list
    }
}
