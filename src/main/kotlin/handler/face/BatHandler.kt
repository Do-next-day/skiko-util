package top.e404.skiko.handler.face

import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
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
    private const val w = 500
    private const val h = 377
    private const val count = 7
    private val range = 0..count
    private val bgList = range.map { getJarImage("statistic/bat/$it.png") }
    private val ddList = DrawData.loadFromJar("statistic/bat/bat.yml")
    private val bgRect = Rect.makeWH(w.toFloat(), h.toFloat())

    override val name = "bat"
    override val regex = Regex("(?i)bat")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.handle { round() }.common(args).replenish(count, Frame::limitAsGif).result {
        handleIndexed { index ->
            Surface.makeRasterN32Premul(w, h).withCanvas {
                drawImageRect(bgList[index], bgRect)
                ddList[index].draw(this, this@handleIndexed)
            }
        }
    }
}