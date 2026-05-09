package top.e404.skiko.handler.face

import org.jetbrains.skia.Matrix33
import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common
import top.e404.skiko.frame.handle
import top.e404.skiko.util.*

@ImageHandler
object LickHandler : FramesHandler {
    private val cover by lazy { getJarImage(this::class.java, "statistic/lick.png") }
    private const val s = 530
    private val matrix by lazy {
        Matrix33(
            0.55220F, 0.09908F, 32.00000F,
            0.05292F, 0.52162F, 189.00000F,
            0.00053F, 0.00000F, 1.00000F,
        )
    }

    override val name = "lick"
    override val regex = Regex("(?i)lick|舔")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.common(args).result {
        handle { image ->
            val center = image.subCenter(s)
            cover.newSurface().withCanvas {
                setMatrix(matrix)
                drawImageRectNearest(center)
                resetMatrix()
                drawImage(cover, 0F, 0F)
            }
        }
    }
}
