@file:Suppress("UNUSED")

package top.e404.skiko

import org.jetbrains.skia.*
import top.e404.skiko.gif.gif
import top.e404.skiko.util.bytes
import top.e404.skiko.util.resize
import kotlin.math.max

/**
 * 从ByteArray解码图片, 若是gif则处理成多个frame
 *
 * @return frames
 */
fun ByteArray.decodeToFrames(): List<Frame> {
    val data = Data.makeFromBytes(this)
    val gif = Codec.makeFromData(data)
    return if (gif.frameCount == 1) listOf(Frame(0, Bitmap().apply {
        allocPixels(gif.imageInfo)
        gif.readPixels(this, 0)
    }).apply {
        duration = 80
    }) else (0 until gif.frameCount).map { index ->
        val bitmap = Bitmap().apply {
            allocPixels(gif.imageInfo)
            gif.readPixels(this, index)
        }
        Frame(gif.getFrameInfo(index).duration, bitmap)
    }
}

/**
 * 将Frames处理成图片, 若只有1帧则处理成png, 否则处理成gif
 *
 * @return 图片的ByteArray
 */
fun List<Frame>.encodeToBytes(): ByteArray {
    return when (size) {
        0 -> throw IllegalArgumentException("gif帧数必须大于0")
        1 -> get(0).bytes(EncodedImageFormat.PNG)
        else -> {
            val bitmap = get(0).bitmap
            gif(bitmap.width, bitmap.height) {
                options {
                    disposalMethod = AnimationDisposalMode.RESTORE_BG_COLOR
                }
                forEach {
                    options {
                        alphaType = if (it.bitmap.computeIsOpaque()) ColorAlphaType.OPAQUE else ColorAlphaType.PREMUL
                    }
                    frame(it.bitmap) {
                        duration = it.duration
                    }
                }
            }.bytes
        }
    }
}

/**
 * 设置每一帧的持续时长
 *
 * @param scale 若为正数则直接设置每一帧的持续时长, 否则按倍率缩放
 * @return gif bytes
 */
fun List<Frame>.scaleDuration(scale: Int): ByteArray {
    if (scale >= 0) forEach { it.duration = scale }
    else forEach { it.duration *= -scale }
    return encodeToBytes()
}

/**
 * 代表gif的一帧
 *
 * @property duration 这一帧的持续时长
 * @property bitmap 图片
 */
class Frame(
    var duration: Int,
    var bitmap: Bitmap,
    var transparency: Boolean? = null,
) {
    fun image() = bitmap.toImage()
    fun bytes(format: EncodedImageFormat = EncodedImageFormat.PNG) = image().bytes(format)
    suspend fun handleAsImage(
        index: Int,
        count: Int,
        data: ExtraData?,
        block: suspend (Int, Int, Image, ExtraData?, Frame) -> Image,
    ) {
        val image = block(index, count, bitmap.toImage(), data, this)
        bitmap = image.toBitmap()
    }

    fun clone() = Frame(duration, bitmap.makeClone(), transparency)
    fun limitAsGif(limit: Float = 600F) {
        val max = max(bitmap.width, bitmap.height)
        if (max < limit) return
        val rate = limit / max
        val w = bitmap.width * rate
        val h = bitmap.height * rate
        bitmap = bitmap.toImage().resize(w.toInt(), h.toInt()).toBitmap()
    }
}