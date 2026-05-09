package top.e404.skiko.handler.list

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Surface
import org.jetbrains.skia.TextLine
import top.e404.skiko.*
import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common
import top.e404.skiko.frame.handle
import top.e404.skiko.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max
import kotlin.math.roundToInt

@ImageHandler
object CharImageHandler : FramesHandler {
    private const val limit = 100F
    private const val base = "圆回田五三十〇彡二一辶丿丶"
    private val font = FontType.YAHEI.getSkiaFont(12F)

    private fun Image.limit(): Image {
        var width = width
        var height = height
        val max = max(width, height)
        if (max > limit) {
            val scale = limit / max
            width = (scale * width).toInt()
            height = (scale * height).toInt()
        }
        return resize(width, height)
    }

    private fun Image.toChars(): List<String> {
        val map = ConcurrentHashMap<Int, String>()
        return limit().toBitmap().run {
            runBlocking(Dispatchers.Default) {
                for (y in 0 until height) launch {
                    val sb = StringBuilder()
                    for (x in 0 until width) {
                        val pixel = getColor(x, y)
                        val a = pixel.alpha()
                        if (a == 0) {
                            sb.append("　")
                            continue
                        }
                        val r = pixel.red() * 0.299F
                        val g = pixel.green() * 0.578f
                        val b = pixel.blue() * 0.114f
                        val index = ((r + g + b) * (base.length + 1) / 255).roundToInt()
                        sb.append(if (index >= base.length) "　" else base[index].toString())
                    }
                    map[y] = sb.toString()
                }
            }
            map.entries.sortedBy { it.key }.map { it.value }
        }
    }

    private fun Image.toColors() = limit().toBitmap().run {
        buildList a@{
            for (y in 0 until height) {
                this@a.add(buildList b@{
                    for (x in 0 until width) this@b.add(getColor(x, y))
                })
            }
        }
    }

    override val name = "字符画"
    override val regex = Regex("(?i)字符画|zfh")

    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.result {
        val bg = args["bg"]?.asColor() ?: Colors.WHITE.argb
        common(args).handle {
            if (args.containsKey("c")) { // 保留颜色
                val text = args["text"]!!
                val sc = args["sc"]?.toFloatOrNull() ?: 1.5F
                val list = it.toColors()
                var i = 0
                val w = 12 * list[0].size
                val h = 12 * list.size
                fun t(): TextLine {
                    if (i >= text.length) i = 0
                    return TextLine.make(text[i++].toString(), font)
                }
                Surface.makeRasterN32Premul(w, h).fill(bg).withCanvas {
                    val paint = Paint()
                    for ((y, l) in list.withIndex()) for ((x, c) in l.withIndex()) {
                        drawTextLine(
                            line = t(),
                            x = x * 12F,
                            y = (y + 1F) * 12,
                            paint = paint.apply {
                                color = c.ahsb().editSaturation {
                                    (it * sc).coerceIn(0F, 1F)
                                }
                            }
                        )
                    }
                }
            } else { // 保留明暗
                val c = args["color"]?.asColor() ?: Colors.BLACK.argb
                val list = it.toChars()
                val texts = list.map { TextLine.make(it, font) }
                val w = texts[0].width
                val h = list.size * 12F
                Surface.makeRasterN32Premul(
                    w.toInt(),
                    h.toInt()
                ).fill(bg).withCanvas {
                    for ((i, s) in texts.withIndex()) drawTextLine(
                        line = s,
                        x = 0F,
                        y = (i + 1) * 12F,
                        paint = Paint().apply { color = c }
                    )
                }
            }
        }
    }
}
