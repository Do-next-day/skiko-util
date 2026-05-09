package top.e404.skiko.handler.face

import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.*
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.util.getJarImage
import top.e404.skiko.util.newSurface
import top.e404.skiko.util.round
import top.e404.skiko.util.withCanvas

@ImageHandler
object CrawlHandler : FramesHandler {
    private const val path = "statistic/crawl/{i}.jpg"
    private val range = 1..73
    private val map = HashMap<Int, Image>()
    private val rect = Rect.makeWH(720F, 720F)

    private fun getBg(index: Int) = map.getOrPut(index) {
        getJarImage(this::class.java, path.replace("{i}", index.toString()))
    }

    override val name = "爬"
    override val regex = Regex("(?i)爬|pa|crawl")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ): HandleResult {
        val change = args.containsKey("c") || args.containsKey("change") // gif每帧都随机一张新的背景
        val count = args["count"]?.toIntOrNull()?.coerceIn(1, 50) ?: 10 // 对图片指定帧数
        if (change) return frames.replenish(count).result {
            handle {
                val bg = getBg(range.random())
                val size = 144
                Surface.makeRasterN32Premul(720, 720).withCanvas {
                    drawImageRect(bg, rect)
                    drawImage(it.round(size), 0F, size * 4F)
                }
            }
        }
        val bg = getBg(range.random())
        return frames.result {
            handle {
                val size = bg.height / 5
                bg.newSurface().withCanvas {
                    drawImage(bg, 0F, 0F)
                    drawImage(it.round(size), 0F, size * 4F)
                }
            }
        }
    }
}
