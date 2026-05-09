package top.e404.skiko.handler.face

import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.handler.DrawData
import top.e404.skiko.util.*

/**
 * 拍
 */
@ImageHandler
object PatMelonHandler : FramesHandler {
    private const val w = 926
    private const val h = 650
    private const val count = 12
    private val range = 0..count
    private val bgList = range.map { getJarImage(this::class.java, "statistic/gua/$it.png") }
    private val ddList = DrawData.loadFromJar("statistic/gua/gua.yml")
    private val bgSrcList = bgList.map { Rect.makeWH(it.width.toFloat(), it.height.toFloat()) }
    private val imgSrc = Rect.makeWH(w.toFloat(), h.toFloat())

    override val name = "Gua"
    override val regex = Regex("(?i)瓜|gua")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.handle { it.subCenter() }.common(args).replenish(count + 1).result {
        pmapIndexed { index ->
            duration = 100
            handleImage {
                val src = Rect.makeWH(it.width.toFloat(), it.height.toFloat())
                Surface.makeRasterN32Premul(w, h).withCanvas {
                    ddList[index].draw(this, image, src)
                    drawImageRectNearest(bgList[index], bgSrcList[index], imgSrc)
                }
            }
        }
    }
}
