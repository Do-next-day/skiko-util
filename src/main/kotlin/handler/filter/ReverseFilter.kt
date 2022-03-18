package top.e404.skiko.handler.filter

import org.jetbrains.skia.Image
import top.e404.skiko.Frame
import top.e404.skiko.ExtraData
import top.e404.skiko.ImageHandler
import top.e404.skiko.handler.argb
import top.e404.skiko.handler.handlePixel

object ReverseFilter : ImageHandler {
    override suspend fun handleFrame(
        index: Int,
        count: Int,
        image: Image,
        data: ExtraData?,
        frame: Frame,
    ) = image.handlePixel(data, handler)

    private val handler = fun(pixel: Int, _: ExtraData?) = pixel.argb().run {
        argb(a, 255 - r, 255 - g, 255 - b)
    }
}