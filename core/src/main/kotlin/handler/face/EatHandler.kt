package top.e404.skiko.handler.face

import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import top.e404.skiko.annotation.ImageHandler
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
    private val bgList = range.map { getJarImage(this::class.java, "statistic/eat/$it.png") }
    private val ddList = DrawData.loadFromJar("statistic/eat/eat.yml")

    override val name = "eat"
    override val regex = Regex("(?i)eat")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.handle { it.round() }.common(args).replenish(count + 1).result {
        pmapIndexed { index ->
            duration = 80
            handleImage {
                val src = Rect.makeWH(it.width.toFloat(), it.height.toFloat())
                Surface.makeRasterN32Premul(w, h).withCanvas {
                    ddList.getOrNull(index)?.draw(this, image, src)
                    drawImage(bgList[index], 0F, 0F)
                }
            }
        }
    }
}
