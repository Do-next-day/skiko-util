package top.e404.skiko.handler.list

import org.jetbrains.skia.IRect
import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.util.*

/**
 * 带状RGB
 */
@ImageHandler
object RgbStripHandler : FramesHandler {
    override val name = "RgbStrip"
    override val regex = Regex("(?i)rgbstrip")
    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ): HandleResult {
        val size = if (frames.size == 1) args["text"]?.toIntOrNull()?.coerceIn(2, 50) ?: 10 else frames.size
        return frames.common(args).replenish(size, Frame::limitAsGif).result {
            val f = args.containsKey("f") // 灰度模式
            val reverse = args.containsKey("r") // 反向
            val img = first().image
            if (args.containsKey("h")) { // 纵向渐变
                val unitHeight = img.height / size // 变换的单位宽度
                if (f) {
                    val ss = args["s"]?.toFloatOrNull() ?: 0.5F
                    val bb = args["b"]?.toFloatOrNull() ?: 0.3F
                    return@result pmapIndexed { index ->
                        val startY = unitHeight * (if (reverse) size - index else index) // 变换起点高度
                        handleImage {
                            val bitmap = it.toBitmap()
                            val result = it.newBitmap()
                            for (y in 0 until img.height) {
                                val currentY = (y + startY) % img.height // 当前处理的y
                                val (hr, hg, hb) = hsb(y.toFloat() / img.height, ss, bb).rgb()
                                for (x in 0 until img.width) {
                                    val pixel = bitmap.getColor(x, currentY)
                                    val (a, r, g, b) = pixel.argb()
                                    if (a == 0) {
                                        result.erase(0, IRect.makeXYWH(x, currentY, 1, 1))
                                        continue
                                    }
                                    result.erase(
                                        argb(a, (r + hr).limit(), (g + hg).limit(), (b + hb).limit()),
                                        IRect.makeXYWH(x, currentY, 1, 1)
                                    )
                                }
                            }
                            result.toImage()
                        }
                    }
                }
                return@result pmapIndexed { index ->
                    val startY = unitHeight * (if (reverse) size - index else index) // 变换起点高度
                    handleImage {
                        val bitmap = it.toBitmap()
                        val result = it.newBitmap()
                        for (y in 0 until img.height) {
                            val currentY = (y + startY) % img.height // 当前处理的y
                            val addH = y.toFloat() / img.height // 增加的h
                            for (x in 0 until img.width) {
                                val pixel = bitmap.getColor(x, currentY)
                                var (a, h, s, b) = pixel.ahsb()
                                if (a == 0) {
                                    result.erase(0, IRect.makeXYWH(x, currentY, 1, 1))
                                    continue
                                }
                                h = (h + addH) % 1
                                result.erase(ahsb(a, h, s, b), IRect.makeXYWH(x, currentY, 1, 1))
                            }
                        }
                        result.toImage()
                    }
                }
            }
            val unitWidth = img.width / size // 变换的单位宽度
            if (f) {
                val ss = args["s"]?.toFloatOrNull() ?: 0.5F
                val bb = args["b"]?.toFloatOrNull() ?: 0.3F
                return@result pmapIndexed { index ->
                    val startX = unitWidth * (if (reverse) size - index else index) // 变换起点宽度
                    handleImage {
                        val bitmap = it.toBitmap()
                        val result = it.newBitmap()
                        for (x in 0 until img.width) {
                            val currentX = (x + startX) % img.width // 当前处理的x
                            val (hr, hg, hb) = hsb(x.toFloat() / img.width, ss, bb).rgb()
                            for (y in 0 until img.height) {
                                val pixel = bitmap.getColor(currentX, y)

                                val (a, r, g, b) = pixel.argb()
                                if (a == 0) {
                                    result.erase(0, IRect.makeXYWH(currentX, y, 1, 1))
                                    continue
                                }
                                result.erase(
                                    argb(a, (r + hr).limit(), (g + hg).limit(), (b + hb).limit()),
                                    IRect.makeXYWH(currentX, y, 1, 1)
                                )
                            }
                        }
                        result.toImage()
                    }
                }
            }
            pmapIndexed { index ->
                val startX = unitWidth * (if (reverse) size - index else index) // 变换起点宽度
                handleImage {
                    val bitmap = it.toBitmap() // 原图
                    val result = it.newBitmap()
                    for (x in 0 until img.width) {
                        val currentX = (x + startX) % img.width // 当前处理的x
                        val addH = x.toFloat() / img.width // 增加的h
                        for (y in 0 until img.height) {
                            val pixel = bitmap.getColor(currentX, y)
                            var (a, h, s, b) = pixel.ahsb()
                            if (a == 0) {
                                result.erase(0, IRect.makeXYWH(currentX, y, 1, 1))
                                continue
                            }
                            h = (h + addH) % 1
                            result.erase(ahsb(a, h, s, b), IRect.makeXYWH(currentX, y, 1, 1))
                        }
                    }
                    result.toImage()
                }
            }
        }
    }
}
