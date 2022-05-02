package top.e404.skiko.handler

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import top.e404.skiko.util.readJarFile
import top.e404.skiko.util.rotateKeepSize

@Serializable
data class DrawData(
    var x: Float,
    var y: Float,
    var w: Float,
    var h: Float,
    var r: Float = 0F,
) {
    companion object {
        fun loadFromJar(path: String) = Yaml.default.decodeFromString<List<DrawData>>(readJarFile(path))
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
    ) = canvas.apply {
        val face = if (r != 0F) image.rotateKeepSize(r) else image
        if (w > 0 && h > 0) drawImageRect(face, Rect.makeXYWH(x, y, w, h))
    }
}