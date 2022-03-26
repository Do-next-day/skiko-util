package top.e404.skiko.handler.face

import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import top.e404.skiko.ExtraData
import top.e404.skiko.Frame
import top.e404.skiko.ImageHandler
import top.e404.skiko.getJarImage
import top.e404.skiko.util.round

object PatHandler : ImageHandler {
    private val bg = getJarImage("statistic/pat.png")
    override suspend fun handleFrame(
        index: Int,
        count: Int,
        image: Image,
        data: ExtraData?,
        frame: Frame,
    ) = image.round().let { face ->
        Surface.makeRaster(bg.imageInfo).run {
            canvas.apply {
                drawImage(bg, 0F, 0F)
                drawImageRect(face,
                    Rect.makeWH(face.width.toFloat(), face.height.toFloat()),
                    Rect.makeXYWH(230F, 270F, 150F, 150F))
            }
            makeImageSnapshot()
        }
    }
}