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
object DislikeHandler : FramesHandler {
    private const val w = 307
    private const val h = 414
    private const val count = 30
    private val range = 0..count
    private val bgList = range.map { getJarImage("statistic/dislike/$it.png") }
    private val ddList = DrawData.loadFromJar("statistic/dislike/dislike.yml")

    override val name = "嫌弃"
    override val regex = Regex("(?i)嫌弃|xq")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.handle { round() }.common(args).replenish(count).result {
        common(args).pmapIndexed { index ->
            handle {
                Surface.makeRasterN32Premul(w, h).withCanvas {
                    drawImage(bgList[index], 0F, 0F)
                    ddList[index].draw(this, image)
                }
            }
        }
    }
}