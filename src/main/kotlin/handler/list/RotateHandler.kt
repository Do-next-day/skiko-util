package top.e404.skiko.handler.list

import org.jetbrains.skia.Image
import top.e404.skiko.ExtraData
import top.e404.skiko.Frame
import top.e404.skiko.ImageHandler
import top.e404.skiko.handler.FloatData
import top.e404.skiko.util.rotate

object RotateHandler : ImageHandler {
    override suspend fun handleFrame(
        index: Int,
        count: Int,
        image: Image,
        data: ExtraData?,
        frame: Frame,
    ): Image {
        var angel = (data as FloatData).data % 360
        if (angel < 0) angel += 360
        // 计算旋转后的零点坐标和图片尺寸
        return image.rotate(angel)
    }
}