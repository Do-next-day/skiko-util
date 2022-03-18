@file:Suppress("UNUSED")

package top.e404.skiko

import org.jetbrains.skia.*
import kotlin.math.PI
import kotlin.math.max
import kotlin.random.Random

fun Surface.bytes(format: EncodedImageFormat = EncodedImageFormat.PNG) = makeImageSnapshot().bytes(format)
fun Image.bytes(format: EncodedImageFormat = EncodedImageFormat.PNG) = encodeToData(format).run {
    requireNotNull(this) { "Image.bytes() return null" }
    bytes
}

/**
 * 裁剪
 *
 * @param rect 裁剪区域
 * @return 裁剪区域的图片
 */
fun Image.sub(rect: Rect): Image {
    return Surface.makeRasterN32Premul(
        rect.width.toInt(),
        rect.height.toInt()
    ).apply {
        canvas.drawImageRect(this@sub, rect)
    }.makeImageSnapshot()
}

/**
 * 使用`双线性插值`和`最邻近过滤`进行图像绘制
 *
 * @see Canvas.drawImageRect
 */
fun Canvas.drawImageRectNearest(
    image: Image,
    src: Rect,
    dst: Rect,
    paint: Paint? = null,
    block: Paint.() -> Unit = {},
) = drawImageRect(
    image,
    src,
    dst,
    FilterMipmap(FilterMode.LINEAR, MipmapMode.NEAREST),
    paint?.apply(block),
    true
)

/**
 * 缩放
 *
 * @param w 新的宽度
 * @param h 新的高度
 * @return 缩放后的图片
 */
fun Image.resize(w: Int, h: Int, smooth: Boolean = true): Image =
    Surface.makeRasterN32Premul(w, h).run {
        canvas.apply {
            scale(w / this@resize.width.toFloat(), h / this@resize.height.toFloat())
            if (smooth) drawImageRectNearest(this@resize,
                Rect.makeWH(this@resize.width.toFloat(), this@resize.height.toFloat()),
                Rect.makeWH(this@resize.width.toFloat(), this@resize.height.toFloat())
            ) else drawImage(this@resize, 0F, 0F)
        }
        makeImageSnapshot()
    }

/**
 * 绘制圆角图片
 *
 * @param image 待绘制的图片
 * @param srcRect 对原图进行裁剪
 * @param rRect 圆角矩形
 */
fun Canvas.drawImageRRect(image: Image, srcRect: Rect, rRect: RRect) {
    // 保存canvas属性
    save()
    // 设置圆角
    clipRRect(rRect, true)
    drawImageRect(image, srcRect, rRect)
    // 恢复canvas属性
    restore()
}

/**
 * 绘制圆角图片
 *
 * @param image 绘制圆角图片
 * @param rRect 圆角矩形
 * @see drawImageRRect
 */
fun Canvas.drawImageRRect(image: Image, rRect: RRect) =
    drawImageRRect(image, Rect.makeWH(image.width.toFloat(), image.height.toFloat()), rRect)

/**
 * 绘制圆角图片
 *
 * @param image 绘制圆角图片
 * @param x 图片左上角的X坐标(距离上方的边距)
 * @param y 图片左上角的Y坐标(距离左侧的边距)
 * @param w 图片宽度
 * @param h 图片高度
 * @param radius 圆角角度
 */
fun Canvas.drawImageRRect(
    image: Image,
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    radius: Float,
) = drawImageRRect(image, RRect.makeXYWH(x, y, w, h, radius))

/**
 * 缩放图片为正方形并且应用圆形蒙版(保留圆形部分)
 *
 * @return 图片
 */
fun Image.round(): Image {
    val size = max(width, height)
    val image = resize(size, size)
    return Surface.makeRasterN32Premul(size, size).run {
        canvas.drawImageRRect(image, 0F, 0F, size.toFloat(), size.toFloat(), size / 2F)
        makeImageSnapshot()
    }
}

fun Double.toRadian() = this * PI / 180
fun Float.toRadian() = this * PI / 180
fun Long.toRadian() = this * PI / 180
fun Int.toRadian() = this * PI / 180

fun <T> List<T>.choose() = get(Random.Default.nextInt(size))
fun <T> MutableList<T>.takeRandom() = removeAt(Random.Default.nextInt(size))

fun Font.descent() = metrics.descent
fun Font.top() = metrics.top
fun Font.height() = metrics.run { size + descent }