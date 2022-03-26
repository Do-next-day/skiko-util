package top.e404.skiko.handler.filter

import org.jetbrains.skia.Image
import org.jetbrains.skia.Matrix33
import org.jetbrains.skia.Surface
import top.e404.skiko.ExtraData
import top.e404.skiko.Frame
import top.e404.skiko.ImageHandler

object FlipVerticalHandler : ImageHandler {
    override suspend fun handleFrame(
        index: Int,
        count: Int,
        image: Image,
        data: ExtraData?,
        frame: Frame,
    ) = Surface.makeRaster(image.imageInfo).run {
        canvas.apply {
            setMatrix(Matrix33(
                -1F, 0F, image.width.toFloat(),
                0F, 1F, 0F,
                0F, 0F, 1F
            ))
            drawImage(image, 0F, 0F)
        }
        makeImageSnapshot()
    }
}