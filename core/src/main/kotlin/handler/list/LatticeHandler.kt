package top.e404.skiko.handler.list

import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.util.*

/**
 * 方格化
 */
@ImageHandler
object LatticeHandler : FramesHandler {
    override val name = "Lattice"
    override val regex = Regex("(?i)lattice|方格化?|fgh?")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ): HandleResult {
        val rate = args["rate"]?.toIntOrNull() ?: 8
        val spacing = args["spacing"]?.toIntOrNull() ?: 3
        val bg = args["bg"]?.asColor()
        return frames.result {
            common(args).handle {
                val temp = run { it.resize(it.width / rate, it.height / rate) }.toBitmap()
                Surface.makeRasterN32Premul(
                    temp.width * (rate + spacing) + spacing,
                    temp.height * (rate + spacing) + spacing
                ).apply {
                    bg?.let { fill(it) }
                }.withCanvas {
                    val paint = Paint()
                    temp.forEach { x, y ->
                        drawRect(Rect.makeXYWH(
                            spacing + x * (rate + spacing).toFloat(),
                            spacing + y * (rate + spacing).toFloat(),
                            rate.toFloat(),
                            rate.toFloat()
                        ), paint.apply {
                            color = temp.getColor(x, y)
                        })
                        false
                    }
                }
            }
        }
    }
}
