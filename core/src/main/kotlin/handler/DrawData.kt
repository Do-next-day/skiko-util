package top.e404.skiko.handler

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import top.e404.skiko.handler.DrawData.Companion.FlipMode.*
import top.e404.skiko.handler.list.FlipHorizontalHandler.flipHorizontal
import top.e404.skiko.handler.list.FlipVerticalHandler.flipVertical
import top.e404.skiko.util.drawImageRectNearest
import top.e404.skiko.util.readJarFile
import top.e404.skiko.util.rotateKeepSize

@Serializable
data class DrawData(
    val x: Float,
    val y: Float,
    val w: Float,
    val h: Float,
    val r: Float = 0F,
    val a: Int = 255,
    val flip: FlipMode = NONE
) {
    companion object {
        fun loadFromJar(path: String) = Yaml.default.decodeFromString<List<DrawData>>(readJarFile(this::class.java, path))

        @Serializable
        enum class FlipMode {
            @SerialName("n")
            NONE,

            @SerialName("h")
            HORIZONTAL,

            @SerialName("v")
            VERTICAL
        }
    }

    /**
     * 在画布上绘制一个图片
     *
     * @param canvas 画布
     * @param image 图片
     * @return 画布
     */
    fun draw(
        canvas: Canvas,
        image: Image,
        src: Rect
    ) = canvas.apply {
        if (w <= 0 || h <= 0) return@apply
        var temp = image
        if (r != 0F) temp = temp.rotateKeepSize(r)
        temp = when (flip) {
            HORIZONTAL -> temp.flipHorizontal()
            VERTICAL -> temp.flipVertical()
            NONE -> temp
        }
        drawImageRectNearest(
            image = temp,
            src = src,
            dst = Rect.makeXYWH(x, y, w, h),
            paint = Paint().apply { alpha = a }
        )
    }
}
