package top.e404.skiko.handler.face

import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import top.e404.skiko.ExtraData
import top.e404.skiko.Frame
import top.e404.skiko.ImageHandler
import top.e404.skiko.getJarImage
import top.e404.skiko.util.subCenter

object DoubtHandler : ImageHandler {
    private val cover = getJarImage("statistic/doubt.png")
    private const val size = 167
    private val faceRect = Rect.makeXYWH(86F, 272F, size.toFloat(), size.toFloat())
    override suspend fun handleFrame(
        index: Int,
        count: Int,
        image: Image,
        data: ExtraData?,
        frame: Frame,
    ) = Surface.makeRaster(cover.imageInfo).run {
        canvas.apply {
            drawImageRect(image.subCenter(size), faceRect)
            drawImage(cover, 0F, 0F)
        }
        makeImageSnapshot()
    }
}