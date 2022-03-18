package top.e404.skiko.handler.filter

import org.jetbrains.skia.Image
import top.e404.skiko.ExtraData
import top.e404.skiko.Frame
import top.e404.skiko.ImageHandler
import top.e404.skiko.handler.IntData
import top.e404.skiko.resize

object PxFilter : ImageHandler {
    override suspend fun handleFrame(
        index: Int,
        count: Int,
        image: Image,
        data: ExtraData?,
        frame: Frame,
    ) = image.run {
        val (scale) = data as IntData
        resize(
            width / scale,
            height / scale,
            false
        ).resize(
            width,
            height,
            false
        )
    }
}