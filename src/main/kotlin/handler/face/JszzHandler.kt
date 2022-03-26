package top.e404.skiko.handler.face

import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import top.e404.skiko.ExtraData
import top.e404.skiko.Frame
import top.e404.skiko.ImageHandler
import top.e404.skiko.getJarImage
import top.e404.skiko.util.rotate
import top.e404.skiko.util.subCenter

object JszzHandler : ImageHandler {
    private val bg = getJarImage("statistic/zz.png")
    override suspend fun handleFrame(
        index: Int,
        count: Int,
        image: Image,
        data: ExtraData?,
        frame: Frame,
    ) = Surface.makeRaster(bg.imageInfo).run {
        canvas.apply {
            drawImage(bg, 0F, 0F)
            val face = image.subCenter().rotate(337F)
            drawImageRect(face,
                Rect.makeWH(face.width.toFloat(), face.height.toFloat()),
                Rect.makeXYWH(-174F, -22F, 1075F, 1075F))
        }
        makeImageSnapshot()
    }
}