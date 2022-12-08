package top.e404.skiko.handler.list

import org.jetbrains.skia.IRect
import top.e404.skiko.apt.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.util.intOrPercentage
import top.e404.skiko.util.toBitmap
import top.e404.skiko.util.toImage
import kotlin.math.min
import kotlin.math.sqrt

@ImageHandler
object DRaiseHandler : FramesHandler {
    override val name = "DRaise"
    override val regex = Regex("(?i)DRaise")
    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ): HandleResult {
        val start = args["start"]?.intOrPercentage(-1) ?: -1 // 1%
        val end = args["end"]?.intOrPercentage(-100) ?: -100 // 100%
        val r = args.containsKey("r") // 循环
        val count = args["text"]?.toIntOrNull() ?: 10 // 总帧数
        return frames.common(args).result {
            replenish(count, Frame::limitAsGif).handleIndexed { index, image ->
                val bitmap = image.toBitmap()
                val result = image.toBitmap()
                val centerX = image.width / 2
                val centerY = image.height / 2
                // 开始的幅度
                val size = min(image.width, image.height)
                val s = if (start <= 0) start * size / -100 else start
                // 最终的幅度
                val e = if (end <= 0) end * size / -100 else end
                val radius = s + (e - s) * index / count
                if (radius == 0) return@handleIndexed result.toImage()
                for (x in 0 until image.width) for (y in 0 until image.height) {
                    val distance = distance(x, y, centerX, centerY)
                    if (distance > radius) continue
                    val tx = (x - centerX) * distance / radius + centerX
                    val ty = (y - centerY) * distance / radius + centerY
                    val color = bitmap.getColor(tx, ty)
                    result.erase(color, IRect.makeXYWH(x, y, 1, 1))
                }
                result.toImage()
            }.also { if (r) for (i in it.size - 2 downTo 1) it.add(it[i]) }
        }
    }

    private fun distance(x1: Int, y1: Int, x2: Int, y2: Int): Int {
        val dx = x1 - x2
        val dy = y1 - y2
        return sqrt((dx * dx + dy * dy).toDouble()).toInt()
    }
}