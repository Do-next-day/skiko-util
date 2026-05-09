package top.e404.skiko.handler.list

import org.jetbrains.skia.IRect
import top.e404.skiko.annotation.ImageHandler
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
        return frames.common(args).result {
            // 总帧数
            val count =
                if (size == 1) args["text"]?.toIntOrNull()?.coerceIn(2, 50) ?: 10
                else size
            // 循环
            if (args.containsKey("r")) {
                val replenish = replenish(count * 2 - 1, Frame::limitAsGif)
                val size = replenish.size
                return@result replenish.handleIndexed { index, image ->
                    val bitmap = image.toBitmap()
                    val result = image.toBitmap()
                    val centerX = image.width / 2
                    val centerY = image.height / 2
                    // 单位幅度
                    val unit = min(image.width, image.height)
                    val startRate =
                        if (start <= 0) start * unit / -100 // 百分比
                        else start // 数值
                    // 最终的幅度
                    val endRate =
                        if (end <= 0) end * unit / -100
                        else end
                    val by =
                        if (index < size / 2) 1 + index
                        else size - index
                    val radius = startRate + (endRate - startRate) * by / size
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
                }
            }
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
            }
        }
    }

    private fun distance(x1: Int, y1: Int, x2: Int, y2: Int): Int {
        val dx = x1 - x2
        val dy = y1 - y2
        return sqrt((dx * dx + dy * dy).toDouble()).toInt()
    }
}