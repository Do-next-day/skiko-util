package top.e404.skiko.util

import org.jetbrains.skia.*
import kotlin.math.*

fun Surface.bytes(format: EncodedImageFormat = EncodedImageFormat.PNG) = makeImageSnapshot().bytes(format)
fun Image.bytes(format: EncodedImageFormat = EncodedImageFormat.PNG) =
    requireNotNull(encodeToData(format)) { "Image.bytes() return null" }.bytes

/**
 * 裁剪
 */
fun Image.sub(
    x: Int,
    y: Int,
    w: Int,
    h: Int
) = Surface.makeRasterN32Premul(w, h).withCanvas {
    drawImageRectNearest(
        image = this@sub,
        src = Rect.makeXYWH(x.toFloat(), y.toFloat(), w.toFloat(), h.toFloat()),
        dst = Rect.makeWH(w.toFloat(), h.toFloat())
    )
}

/**
 * 使用`双线性插值`和`最邻近过滤`进行图像绘制
 *
 * @see Canvas.drawImageRect
 */
fun Canvas.drawImageRectNearest(
    image: Image,
    src: Rect = Rect.makeWH(image.width.toFloat(), image.height.toFloat()),
    dst: Rect = src,
    paint: Paint? = null,
    block: Paint.() -> Unit = {},
) = drawImageRect(
    image,
    src,
    dst,
    FilterMipmap(FilterMode.LINEAR, MipmapMode.NEAREST),
    paint?.apply(block),
    false
)

/**
 * 缩放
 *
 * @param w 新的宽度, 若小于0则作为百分比处理
 * @param h 新的高度, 若小于0则作为百分比处理
 * @return 缩放后的图片
 */
fun Image.resize(w: Int, h: Int, smooth: Boolean = false): Image {
    require(w != 0) { "图片宽度不可为0" }
    require(h != 0) { "图片高度不可为0" }
    val width = if (w > 0) w else (w / -100.0 * width).toInt()
    val height = if (h > 0) h else (h / -100.0 * height).toInt()
    return Surface.makeRasterN32Premul(width, height).withCanvas {
        scale(width / this@resize.width.toFloat(), height / this@resize.height.toFloat())
        if (smooth) {
            drawImage(this@resize, 0F, 0F)
            return@withCanvas
        }
        drawImageRectNearest(
            image = this@resize,
            src = Rect.makeWH(this@resize.width.toFloat(), this@resize.height.toFloat()),
            dst = Rect.makeWH(this@resize.width.toFloat(), this@resize.height.toFloat())
        )
    }
}

/**
 * 绘制圆角图片
 *
 * @param image 待绘制的图片
 * @param src 对原图进行裁剪
 * @param dst 圆角矩形
 */
fun Canvas.drawImageRRect(
    image: Image,
    src: Rect,
    dst: RRect,
    samplingMode: FilterMipmap = FilterMipmap(FilterMode.LINEAR, MipmapMode.NEAREST),
    paint: Paint? = null,
    strict: Boolean = true
) {
    // 保存canvas属性
    save()
    // 设置圆角
    clipRRect(dst, true)
    drawImageRect(
        image = image,
        src = src,
        dst = dst,
        samplingMode = samplingMode,
        paint = paint,
        strict = strict
    )
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
 * 裁剪图片中间的正方形图片并且应用圆形蒙版(保留圆形部分)
 *
 * @param size 尺寸
 * @return 图片
 */
fun Image.round(size: Int? = null): Image {
    val s = size ?: max(width, height)
    return Surface.makeRasterN32Premul(s, s).withCanvas {
        drawImageRRect(subCenter(s), 0F, 0F, s.toFloat(), s.toFloat(), s / 2F)
    }
}

/**
 * 从图片中间截取一块正方形
 *
 * @param size 尺寸
 * @return 图片
 */
fun Image.subCenter(size: Int? = null): Image {
    val min = min(width, height)
    val x = abs(width - min) / 2
    val y = abs(height - min) / 2
    return sub(x, y, min, min).run {
        size?.let { s -> resize(s, s) } ?: this
    }
}

/**
 * 旋转
 *
 * @param angel 角度
 * @return 图片(尺寸比原图大)
 */
fun Image.rotate(angel: Float): Image {
    var a = angel % 360
    if (a < 0) a += 360
    val r = a.toRadian()
    // 计算旋转后的零点坐标和图片尺寸
    val x: Float
    val y: Float
    val w: Int
    val h: Int
    when (a) {
        in 0F..90F -> {
            w = (cos(r) * width + sin(r) * height).toInt()
            h = (sin(r) * width + cos(r) * height).toInt()
            x = sin(r).toFloat() * height
            y = 0F
        }

        in 90F..180F -> {
            val ta = (a - 90).toRadian()
            w = (sin(ta) * width + cos(ta) * height).toInt()
            h = (cos(ta) * width + sin(ta) * height).toInt()
            x = w.toFloat()
            y = (sin(ta) * height).toFloat()
        }

        in 180F..270F -> {
            val ta = (a - 180).toRadian()
            w = (cos(ta) * width + sin(ta) * height).toInt()
            h = (sin(ta) * width + cos(ta) * height).toInt()
            x = (cos(ta) * width).toFloat()
            y = h.toFloat()
        }

        else -> {
            val ta = (a - 270).toRadian()
            w = (sin(ta) * width + cos(ta) * height).toInt()
            h = (cos(ta) * width + sin(ta) * height).toInt()
            x = 0F
            y = (cos(ta) * width).toFloat()
        }
    }
    return Surface.makeRasterN32Premul(w, h).run {
        canvas.apply {
            // 位移
            translate(x, y)
            // 设置旋转
            rotate(a)
            // 绘制
            drawImage(this@rotate, 0F, 0F)
        }
        makeImageSnapshot()
    }
}

/**
 * 旋转后裁剪为原图大小
 *
 * @param angel 角度
 * @return 图片
 */
fun Image.rotateKeepSize(
    angel: Float
) = rotate(angel).let {
    it.sub(
        abs((it.width - width) / 2),
        abs((it.height - height) / 2),
        width,
        height
    )
}

fun Surface.fill(color: Int) = apply { canvas.clear(color) }

fun Surface.withCanvas(block: Canvas.() -> Unit): Image {
    canvas.block()
    return makeImageSnapshot()
}

fun Image.newBitmap(edit: Int = 0) = Bitmap().also {
    it.allocPixels(
        ImageInfo(
            width = imageInfo.width + edit,
            height = imageInfo.height + edit,
            colorType = ColorType.BGRA_8888,
            alphaType = ColorAlphaType.PREMUL,
            colorSpace = imageInfo.colorSpace
        )
    )
}

fun Bitmap.newBitmap(edit: Int = 0) = Bitmap().also {
    it.allocPixels(
        ImageInfo(
            width = imageInfo.width + edit,
            height = imageInfo.height + edit,
            colorType = ColorType.BGRA_8888,
            alphaType = ColorAlphaType.PREMUL,
            colorSpace = imageInfo.colorSpace
        )
    )
}
