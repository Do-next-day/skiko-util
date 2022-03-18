package top.e404.skiko.handler.filter

import org.jetbrains.skia.Image
import org.jetbrains.skia.Surface
import top.e404.skiko.Frame
import top.e404.skiko.ExtraData
import top.e404.skiko.ImageHandler
import top.e404.skiko.handler.FloatData
import top.e404.skiko.toRadian
import kotlin.math.cos
import kotlin.math.sin

object RotateFilter : ImageHandler {
    override suspend fun handleFrame(
        index: Int,
        count: Int,
        image: Image,
        data: ExtraData?,
        frame: Frame,
    ): Image {
        var angel = (data as FloatData).data % 360
        if (angel < 0) angel += 360
        val r = angel.toRadian()
        // 计算旋转后的零点坐标和图片尺寸
        val x: Float
        val y: Float
        val w: Int
        val h: Int
        when (angel) {
            in 0F..90F -> {
                w = (cos(r) * image.width + sin(r) * image.height).toInt()
                h = (sin(r) * image.width + cos(r) * image.height).toInt()
                x = sin(r).toFloat() * image.height
                y = 0F
            }
            in 90F..180F -> {
                val a = (angel - 90).toRadian()
                w = (sin(a) * image.width + cos(a) * image.height).toInt()
                h = (cos(a) * image.width + sin(a) * image.height).toInt()
                x = w.toFloat()
                y = (sin(a) * image.height).toFloat()
            }
            in 180F..270F -> {
                val a = (angel - 180).toRadian()
                w = (cos(a) * image.width + sin(a) * image.height).toInt()
                h = (sin(a) * image.width + cos(a) * image.height).toInt()
                x = (cos(a) * image.width).toFloat()
                y = h.toFloat()
            }
            else -> {
                val a = (angel - 270).toRadian()
                w = (sin(a) * image.width + cos(a) * image.height).toInt()
                h = (cos(a) * image.width + sin(a) * image.height).toInt()
                x = 0F
                y = (cos(a) * image.width).toFloat()
            }
        }
        return Surface.makeRasterN32Premul(w, h).run {
            canvas.apply {
                // 位移
                translate(x, y)
                // 设置旋转
                rotate(angel)
                // 绘制
                drawImage(image, 0F, 0F)
            }
            makeImageSnapshot()
        }
    }
}