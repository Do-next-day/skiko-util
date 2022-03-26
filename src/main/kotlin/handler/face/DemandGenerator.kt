package top.e404.skiko.handler.face

import org.jetbrains.skia.Image
import org.jetbrains.skia.Surface
import top.e404.skiko.ExtraData
import top.e404.skiko.Frame
import top.e404.skiko.ImageHandler
import top.e404.skiko.getJarImage
import top.e404.skiko.util.round

object DemandGenerator : ImageHandler {
    private val bg = getJarImage("statistic/demand.jpg")
    override suspend fun handleFrame(index: Int, count: Int, image: Image, data: ExtraData?, frame: Frame) =
        Surface.makeRaster(bg.imageInfo).run {
            canvas.apply {
                drawImage(bg, 0F, 0F)
                drawImage(image.round(90), 107F, 37F)
            }
            makeImageSnapshot()
        }
}