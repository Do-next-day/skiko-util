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
 * 啃
 */
@ImageHandler
object EatHandler : FramesHandler {
    private const val w = 362
    private const val h = 364
    private const val count = 15
    private val range = 0..count
    private val bgList = range.map { getJarImage("statistic/eat/$it.png") }
    private val ddList = DrawData.loadFromJar("statistic/eat/eat.yml")

    override val name = "eat"
    override val regex = Regex("(?i)eat")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.handle { round() }.common(args).replenish(count).result {
        common(args).pmapIndexed { index ->
            duration = 80
            handle {
                Surface.makeRasterN32Premul(w, h).withCanvas {
                    ddList.getOrNull(index)?.draw(this, image)
                    drawImage(bgList[index], 0F, 0F)
                }
            }
        }
    }
}