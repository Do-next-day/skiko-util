package top.e404.skiko.handler.list

import org.jetbrains.skia.Matrix33
import org.jetbrains.skia.Surface
import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.util.drawImageRectNearest
import top.e404.skiko.util.newSurface
import top.e404.skiko.util.round
import top.e404.skiko.util.withCanvas
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * 生成转动gif
 */
@ImageHandler
object TurnHandler : FramesHandler {
    override val name = "转动"
    override val regex = Regex("转动?|(?i)zhuan|turn|zd")
    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ): HandleResult {
        val count = args["text"]?.toIntOrNull()?.coerceIn(-50, 50) ?: 10
        val r = args.containsKey("r")
        return frames.common(args).replenish(abs(count), Frame::limitAsGif).result {
            val v = (360F / size).let { if (count < 0) -it else it }
            handleIndexed { index, image ->
                if (r) {
                    // 不应用圆形蒙版
                    val size = first().run { sqrt(1.0 * image.width * image.width + image.height * image.height) }.toInt()
                    return@handleIndexed Surface.makeRasterN32Premul(size, size).withCanvas {
                        setMatrix(
                            Matrix33
                                .makeTranslate((size - image.width) / 2F, (size - image.height) / 2F)
                                .makeConcat(Matrix33.makeTranslate(image.width / 2F, image.height / 2F))
                                .makeConcat(Matrix33.makeRotate(index * v))
                                .makeConcat(Matrix33.makeTranslate(image.width / -2F, image.height / -2F))
                        )
                        drawImageRectNearest(image)
                    }
                }
                // 不应用圆形蒙版
                val i = image.round()
                i.newSurface().withCanvas {
                    setMatrix(
                        Matrix33
                            .makeTranslate(i.width / 2F, i.height / 2F)
                            .makeConcat(Matrix33.makeRotate(index * v))
                            .makeConcat(Matrix33.makeTranslate(i.width / -2F, i.height / -2F))
                    )
                    drawImageRectNearest(i)
                }
            }
        }
    }
}
