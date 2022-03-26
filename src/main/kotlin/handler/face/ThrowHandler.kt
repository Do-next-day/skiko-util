package top.e404.skiko.handler.face

import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import top.e404.skiko.ExtraData
import top.e404.skiko.Frame
import top.e404.skiko.ImageHandler
import top.e404.skiko.getJarImage
import top.e404.skiko.util.rotateKeepSize
import top.e404.skiko.util.round

object ThrowHandler : ImageHandler {
    private const val size = 448
    private val bg = getJarImage("statistic/throw.png")

    override suspend fun handleFrame(
        index: Int,
        count: Int,
        image: Image,
        data: ExtraData?,
        frame: Frame,
    ) = Surface.makeRasterN32Premul(size, size).run {
        val face = image.round().rotateKeepSize(270F)
        canvas.apply {
            drawImage(bg, 0F, 0F)
            drawImageRect(face,
                Rect.makeWH(face.width.toFloat(), face.height.toFloat()),
                Rect.makeXYWH(10F, 175F, 150F, 150F)
            )
        }
        makeImageSnapshot()
    }
}