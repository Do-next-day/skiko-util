@file:Suppress("UNUSED")

package top.e404.skiko

import org.jetbrains.skia.*
import top.e404.skiko.gif.DisposalMethod
import top.e404.skiko.gif.buildGif

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
    })) else (0 until gif.frameCount).map { index ->
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
            buildGif(bitmap.width, bitmap.height) {
                options {
                    method = DisposalMethod.RESTORE_TO_BACKGROUND
                    transparency = true
                }
                forEach {
                    frame(it.bitmap) {
                        it.transparency?.let { t -> transparency = t }
                        duration = it.duration
                    }
                }
            }
        }
    }
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
    fun image() = Image.makeFromBitmap(bitmap)
    fun bytes(format: EncodedImageFormat = EncodedImageFormat.PNG) = image().bytes(format)
    suspend fun handleAsImage(
        index: Int,
        count: Int,
        data: ExtraData?,
        block: suspend (Int, Int, Image, ExtraData?, Frame) -> Image,
    ) {
        val image = block(index, count, Image.makeFromBitmap(bitmap), data, this)
        bitmap = Bitmap.makeFromImage(image)
    }

    fun clone() = Frame(duration, bitmap.makeClone(), transparency)
}