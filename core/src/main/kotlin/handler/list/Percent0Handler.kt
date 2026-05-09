package top.e404.skiko.handler.list

import org.jetbrains.skia.*
import top.e404.skiko.FontType
import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.util.pmapIndexed
import top.e404.skiko.util.withCanvas
import kotlin.math.abs
import kotlin.math.min

/**
 * 0%
 */
@ImageHandler
object Percent0Handler : FramesHandler {
    override val name = "Percent0"
    override val regex = Regex("(\\d{1,2}|100)%")
    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ): HandleResult {
        val t = args["command_name"] ?: "0%"
        return frames.replenish(20, Frame::limitAsGif).result {
            common(args).pmapIndexed { index ->
                val center = size / 2
                handleImage {
                    val w = it.width / 2f
                    val h = it.height / 2f
                    val radius = min(w, h) * .24f
                    val text = TextLine.make(t, FontType.MI.getSkiaFont(radius * .7f))
                    val v = (abs(center - index) + 1).toFloat() / size / 4
                    Surface.makeRaster(it.imageInfo).withCanvas {
                        val paint = Paint().apply {
                            isAntiAlias = true
                            color = Color.WHITE
                        }
                        clear(Color.BLACK)
                        drawImage(it, 0F, 0F, paint.apply { alpha = 160 })
                        drawCircle(w, h, radius, paint.apply {
                            mode = PaintMode.STROKE
                            strokeWidth = radius * .17f
                            maskFilter = MaskFilter.makeBlur(FilterBlurMode.SOLID, radius * v)
                            alpha = 200
                        })
                        drawTextLine(text, w - text.width / 2, h + text.height / 4, paint.apply {
                            mode = PaintMode.FILL
                            maskFilter = null
                        })
                    }
                }
            }
        }
    }
}
