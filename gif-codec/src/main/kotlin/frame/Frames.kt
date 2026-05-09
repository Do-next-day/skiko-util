package top.e404.skiko.frame

import org.jetbrains.skia.*
import top.e404.skiko.gif.gif
import top.e404.skiko.util.*
import kotlin.math.max

/**
 * 从ByteArray解码图片, 若是gif则处理成多个frame
 *
 * @return frames
 */
fun ByteArray.decodeToFrames(): MutableList<Frame> {
    val codec = Codec.makeFromData(Data.makeFromBytes(this))
    val b = codec.frameCount == 1
    return (0 until codec.frameCount).map { index ->
        Frame(
            if (b) 50 else codec.getFrameInfo(index).duration,
            Bitmap().apply {
                allocPixels(codec.imageInfo)
                codec.readPixels(this, index)
            }.toImage()
        )
    }.toMutableList()
}

/**
 * 将Frames处理成图片, 若只有1帧则处理成png, 否则处理成gif
 *
 * @return 图片的ByteArray
 */
fun List<Frame>.encodeToBytes() = when (size) {
    0 -> throw IllegalArgumentException("gif帧数必须大于0")
    1 -> get(0).bytes(EncodedImageFormat.PNG)
    else -> {
        val image = get(0).image
        gif(image.width, image.height) {
            options {
                disposalMethod = AnimationDisposalMode.RESTORE_BG_COLOR
            }
            forEach {
                val bitmap = it.toBitmap()
                options {
                    alphaType = if (bitmap.computeIsOpaque()) ColorAlphaType.OPAQUE
                    else ColorAlphaType.PREMUL
                }
                frame(bitmap) { duration = it.duration }
            }
        }.bytes
    }
}

/**
 * 对frames的每一帧进行处理
 *
 * @param block 处理
 * @return frames
 */
suspend fun List<Frame>.handle(
    block: (Image) -> Image
) = pmap { handleImage(block) }

/**
 * 对frames的每一帧进行处理, 处理时获取frame下标
 *
 * @param block 处理
 * @return frames
 */
suspend fun List<Frame>.handleIndexed(
    block: (Int, Image) -> Image
) = pmapIndexed { handleImage { img -> block(it, img) } }

/**
 * 处理通用参数
 *
 * @param args 参数
 * @return frames
 */
fun MutableList<Frame>.common(
    args: Map<String, String>
) = apply {
    // 每一帧的持续时长 单位ms
    val duration = args["d"]?.toIntOrNull()
    // 图片宽度
    val width = args["w"]?.toIntOrNull()
    // 图片高度
    val height = args["h"]?.toIntOrNull()
    onEach { frame ->
        duration?.let { frame.duration = it }
        if (width == null && height == null) return@onEach
        frame.handleImage {
            val w = width ?: it.width
            val h = height ?: it.height
            it.resize(w, h, false)
        }
    }
}

/**
 * 处理每一个frame的图片内容
 *
 * @param block 处理
 * @return frames
 */
suspend fun List<Frame>.withCanvas(
    block: Canvas.(Image) -> Unit
) = pmap {
    apply {
        image = image.newSurface().withCanvas {
            block(image)
        }
    }
}

/**
 * 增加帧数
 *
 * @param count 目标帧数
 * @param block 在增加帧数之前进行的操作
 */
suspend fun MutableList<Frame>.replenish(
    count: Int,
    block: Frame.() -> Unit = {}
) = run {
    pmap(block)
    if (size >= count) return@run this
    var i = 0
    (1..count).map {
        i++
        if (i >= size) i = 0
        this[i].clone()
    }.toMutableList()
}

fun Image.toFrame(duration: Int = 0) = Frame(duration, this)
fun Image.toFrames() = mutableListOf(toFrame())

/**
 * 代表gif的一帧
 *
 * @property duration 这一帧的持续时长
 * @property image 图片
 */
class Frame(
    var duration: Int,
    var image: Image,
) {
    fun toBitmap() = image.toBitmap()
    fun duration(duration: Int) = apply { this.duration = duration }
    fun image(image: Image) = apply { this.image = image }
    fun bytes(format: EncodedImageFormat = EncodedImageFormat.PNG) = image.bytes(format)
    fun handleImage(block: (Image) -> Image) = apply { image = block(image) }
    fun clone() = Frame(duration, image.toBitmap().makeClone().toImage())
    fun limitAsGif(limit: Float = 600F) {
        val max = max(image.width, image.height)
        if (max < limit) return
        val rate = limit / max
        val w = image.width * rate
        val h = image.height * rate
        image = image.resize(w.toInt(), h.toInt())
    }
}
