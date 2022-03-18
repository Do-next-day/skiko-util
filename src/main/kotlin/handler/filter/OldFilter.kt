package top.e404.skiko.handler.filter

import org.jetbrains.skia.Image
import top.e404.skiko.Frame
import top.e404.skiko.ExtraData
import top.e404.skiko.ImageHandler
import top.e404.skiko.handler.argb
import top.e404.skiko.handler.handlePixel

object OldFilter : ImageHandler {
    override suspend fun handleFrame(
        index: Int,
        count: Int,
        image: Image,
        data: ExtraData?,
        frame: Frame,
    ) = image.handlePixel(data, handler)

    private val handler = fun(pixel: Int, _: ExtraData?): Int {
        val (a, r, g, b) = pixel.argb()
        if (a == 0) return 0
        val nb = 0.272 * r + 0.543 * g + 0.131 * b
        val ng = 0.349 * r + 0.686 * g + 0.168 * b
        val nr = 0.393 * r + 0.769 * g + 0.189 * b
        return argb(a, nr, ng, nb)
    }
}