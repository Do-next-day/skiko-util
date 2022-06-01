package top.e404.skiko.handler.face

import org.jetbrains.skia.Surface
import top.e404.skiko.apt.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.handler.DrawData
import top.e404.skiko.util.getJarImage
import top.e404.skiko.util.pmapIndexed
import top.e404.skiko.util.round
import top.e404.skiko.util.withCanvas

/**
 * 嫌弃
 */
@ImageHandler
object FondleHandler : FramesHandler {
    private const val w = 100
    private const val h = 110
    private const val count = 28
    private val range = 0..count
    private val bgList = range.map { getJarImage("statistic/fondle/$it.png") }
    private val ddList = DrawData.loadFromJar("statistic/fondle/fondle.yml")

    override val name = "fondle"
    override val regex = Regex("(?i)fondle")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.handle { round() }.common(args).replenish(count, Frame::limitAsGif).result {
        common(args).pmapIndexed { index ->
            duration(80)
            handle {
                Surface.makeRasterN32Premul(w, h).withCanvas {
                    drawImage(bgList[index], 0F, 0F)
                    ddList[index].draw(this, image)
                }
            }
        }
    }
}