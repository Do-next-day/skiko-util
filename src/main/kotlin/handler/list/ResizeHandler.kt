package top.e404.skiko.handler.filter

import org.jetbrains.skia.Image
import top.e404.skiko.ExtraData
import top.e404.skiko.Frame
import top.e404.skiko.ImageHandler
import top.e404.skiko.handler.IntPairData
import top.e404.skiko.util.resize

/**
 * 若数字为负数则作为百分比处理
 */
object ResizeHandler : ImageHandler {
    override suspend fun handleFrame(
        index: Int,
        count: Int,
        image: Image,
        data: ExtraData?,
        frame: Frame,
    ): Image {
        var (w, h) = data as IntPairData
        if (w < 0) w = (w / -100.0 * image.width).toInt()
        if (h < 0) h = (h / -100.0 * image.height).toInt()
        return image.resize(w, h)
    }
}