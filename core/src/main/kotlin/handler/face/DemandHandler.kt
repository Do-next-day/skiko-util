package top.e404.skiko.handler.face

import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common
import top.e404.skiko.util.getJarImage
import top.e404.skiko.util.newSurface
import top.e404.skiko.util.round
import top.e404.skiko.util.withCanvas

/**
 * 白嫖
 */
@ImageHandler
object DemandHandler : FramesHandler {
    private val bg = getJarImage(this::class.java, "statistic/demand.jpg")

    override val name = "白嫖"
    override val regex = Regex("(?i)白嫖|bp")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.result {
        common(args).onEach { frame ->
            frame.handleImage {
                bg.newSurface().withCanvas {
                    drawImage(bg, 0F, 0F)
                    drawImage(it.round(90), 107F, 37F)
                }
            }
        }
    }
}
