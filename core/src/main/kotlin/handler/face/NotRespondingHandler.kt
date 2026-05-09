package top.e404.skiko.handler.face

import org.jetbrains.skia.*
import top.e404.skiko.util.Colors
import top.e404.skiko.FontType
import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common
import top.e404.skiko.frame.handle
import top.e404.skiko.util.getJarImage
import top.e404.skiko.util.withCanvas

@ImageHandler
object NotRespondingHandler : FramesHandler {
    private val rt = getJarImage(this::class.java, "statistic/not_responding.png")
    private val rect by lazy { Rect.makeXYWH(2F, 3F, 16F, 16F) }
    private val colorPaint by lazy {
        Paint().apply {
            colorFilter = ColorFilter.makeMatrix(
                ColorMatrix(
                    .3F, 0F, 0F, 0F, .7F,
                    0F, .3F, 0F, 0F, .7F,
                    0F, 0F, .3F, 0F, .7F,
                    0F, 0F, 0F, 1F, 0F,
                )
            )
        }
    }
    private val whitePaint by lazy {
        Paint().apply {
            color = Colors.WHITE.argb
        }
    }
    private val font by lazy {
        FontType.YAHEI.getSkiaFont(14F)
    }

    override val name = "NotResponding"
    override val regex = Regex("未响应|无响应|(?i)NotResponding|wxy")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.result {
        val title = args["text"] ?: "（未响应）"
        listOf(common(args)[0]).handle { image ->
            Surface.makeRasterN32Premul(image.width, image.height + 23).withCanvas {
                clear(Colors.BLACK.argb)
                // 主体
                drawImage(image, 0F, 23F, colorPaint)
                // 图标
                drawImageRect(image, rect)
                // 文本
                drawTextLine(TextLine.make(title, font), 24F, 17F, whitePaint)
                // 右上角图标
                drawImage(rt, (image.width - rt.width).toFloat(), 0F)
            }
        }
    }
}
