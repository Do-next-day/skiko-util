package top.e404.skiko.handler.list

import org.jetbrains.skia.Image
import top.e404.skiko.ExtraData
import top.e404.skiko.Frame
import top.e404.skiko.ImageHandler
import top.e404.skiko.util.round

object RoundHandler : ImageHandler {
    override suspend fun handleFrame(
        index: Int,
        count: Int,
        image: Image,
        data: ExtraData?,
        frame: Frame,
    ) = image.round()
}