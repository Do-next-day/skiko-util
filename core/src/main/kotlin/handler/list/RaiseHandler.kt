package top.e404.skiko.handler.list

import org.jetbrains.skia.IRect
import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.util.doubleOrPercentage
import top.e404.skiko.util.toBitmap
import top.e404.skiko.util.toImage
import kotlin.math.min
import kotlin.math.sqrt

@ImageHandler
object RaiseHandler : FramesHandler {
    override val name = "raise"
    override val regex = Regex("(?i)raise")
    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ): HandleResult {
        val radius = args["text"].doubleOrPercentage(null)
        return frames.result {
            common(args).handle {
                val bitmap = it.toBitmap()
                val result = it.toBitmap()
                val centerX = it.width / 2
                val centerY = it.height / 2
                val r = when {
                    radius == null -> min(it.width, it.height) / 2
                    radius < 0 -> min(it.width, it.height) * -radius / 100
                    else -> radius
                }.toInt()
                for (x in 0 until it.width) for (y in 0 until it.height) {
                    val distance = distance(x, y, centerX, centerY)
                    if (distance > r) continue
                    val tx = (x - centerX) * distance / r + centerX
                    val ty = (y - centerY) * distance / r + centerY
                    val color = bitmap.getColor(tx, ty)
                    result.erase(color, IRect.makeXYWH(x, y, 1, 1))
                }
                result.toImage()
            }
        }
    }

    private fun distance(x1: Int, y1: Int, x2: Int, y2: Int): Int {
        val dx = x1 - x2
        val dy = y1 - y2
        return sqrt((dx * dx + dy * dy).toDouble()).toInt()
    }
}
