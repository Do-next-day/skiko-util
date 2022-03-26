package top.e404.skiko.handler.list

import org.jetbrains.skia.Image
import top.e404.skiko.ExtraData
import top.e404.skiko.Frame
import top.e404.skiko.ImageHandler
import top.e404.skiko.handler.IntPairData
import top.e404.skiko.util.resize

object ClipHandler : ImageHandler {
    override suspend fun handleFrame(
        index: Int,
        count: Int,
        image: Image,
        data: ExtraData?,
        frame: Frame,
    ): Image {
        val (w, h) = data as IntPairData
        require(w != 0) { "目标宽度不可为0" }
        require(h != 0) { "目标高度不可为0" }
        val nw = if (w < 0) image.width * -w / 100 else w
        val nh = if (h < 0) image.height * -h / 100 else h
        return image.resize(nw, nh)
    }
}