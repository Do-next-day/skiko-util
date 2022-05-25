package top.e404.skiko.handler.face

import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import top.e404.skiko.Colors
import top.e404.skiko.apt.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.handler.DrawData
import top.e404.skiko.util.getJarImage
import top.e404.skiko.util.round
import top.e404.skiko.util.withCanvas

/**
 * 球拍
 */
@ImageHandler
object BatHandler : FramesHandler {
    private const val width = 500
    private const val height = 377
    private val range = 0..7
    private val bgList = range.map { getJarImage("statistic/bat/$it.png") }
    private val ddList = DrawData.loadFromJar("statistic/bat/bat.yml")
    private val bgRect = Rect.makeWH(width.toFloat(), height.toFloat())
    private val paint = Paint().apply { color = Colors.WHITE.argb }

    override val name = "bat"
    override val regex = Regex("(?i)bat")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ): HandleResult {
        var i = 0
        frames.handle { round() }
        val fs = range.map {
            i++
            if (i >= frames.size) i = 0
            frames[i].clone()
        }.toMutableList()
        return fs.result {
            handleIndexed { index ->
                Surface.makeRasterN32Premul(
                    this@BatHandler.width,
                    this@BatHandler.height
                ).withCanvas {
                    drawRect(bgRect, paint)
                    drawImageRect(bgList[index], bgRect)
                    ddList[index].draw(this, this@handleIndexed)
                }
            }
        }
    }
}