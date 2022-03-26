@file:Suppress("UNUSED")

package top.e404.skiko.handler.list

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.*
import top.e404.skiko.*
import top.e404.skiko.util.resize
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import kotlin.math.max
import kotlin.math.roundToInt

object CharImageHandler : ImageHandler {
    private const val base = "圆回田五三十〇彡二一辶丿丶"
    private val font = FontType.YAHEI.getSkijaFont(12F)
    private val wp = Paint().apply { color = Colors.WHITE.argb }
    private val bp = Paint().apply { color = Colors.BLACK.argb }
    private fun Image.toChars(): List<String> {
        var width = width
        var height = height
        val max = max(width, height)
        if (max > 50) {
            val scale = 50.0 / max
            width = (scale * width).toInt()
            height = (scale * height).toInt()
        }
        val map = ConcurrentHashMap<Int, String>()
        val list = Bitmap.makeFromImage(resize(width, height)).run {
            val count = if (height > 200) 200 else height
            //Dispatchers.Default.limitedParallelism(count).
            val p = Executors.newScheduledThreadPool(count)
            p.asCoroutineDispatcher().use { dispatcher ->
                runBlocking(dispatcher) {
                    for ((i, y) in (0 until width).withIndex()) launch {
                        val sb = StringBuilder()
                        for (x in 0 until height) {
                            val pixel = getColor(x, y)
                            val a = pixel and 0xff000000.toInt() shr 24
                            if (a == 0) {
                                sb.append("　")
                                continue
                            }
                            val r = (pixel and 0xff0000 shr 16) * 0.299F
                            val g = (pixel and 0xff00 shr 8) * 0.578f
                            val b = (pixel and 0xff) * 0.114f
                            val index = ((r + g + b) * (base.length + 1) / 255).roundToInt()
                            sb.append(if (index >= base.length) "　" else base[index].toString())
                        }
                        map[i] = sb.toString()
                    }
                }
                map.entries.sortedBy { it.key }.map { it.value }
            }
        }
        return list
    }

    override suspend fun handleFrame(
        index: Int,
        count: Int,
        image: Image,
        data: ExtraData?,
        frame: Frame,
    ): Image {
        frame.transparency = false
        val list = image.toChars()
        val texts = list.map { TextLine.make(it, font) }
        val w = texts[0].width
        val h = list[0].length * 12F
        return Surface.makeRasterN32Premul(w.toInt(), h.toInt()).run {
            canvas.apply {
                drawRect(Rect.makeXYWH(0F, 0F, w, h), wp)
                for ((i, s) in texts.withIndex()) drawTextLine(s, 0F, (i + 1) * 12F, bp)
            }
            makeImageSnapshot()
        }
    }
}