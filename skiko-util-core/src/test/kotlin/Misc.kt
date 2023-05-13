package top.e404.skiko

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.junit.Test
import top.e404.skiko.handler.list.EmbossHandler
import top.e404.skiko.util.*
import java.io.File
import kotlin.math.max
import kotlin.math.pow

class Misc {
    private val limit = 220
    private val paint = Paint().apply { colorFilter = grayMatrix }

    @Test
    fun t1() {
        File("in").listFiles()!!.forEach {
            if (it.name.endsWith(".gif")) return@forEach
            val src = Image.makeFromEncoded(it.readBytes())
            val img = src.newSurface()
                .withCanvas { drawImage(src, 0F, 0F, paint) }
                .let { img ->
                    if (img.width > limit || img.height > limit) {
                        val size = max(img.width, img.height)
                        val rate = limit.toFloat() / size
                        val w = (img.width * rate).toInt()
                        val h = (img.height * rate).toInt()
                        println("${it.name}: $w * $h")
                        img.resize(w, h)
                    } else img
                }
                .toBitmap()
                .binary()
            println(con(img, 1, 1))
        }
    }

    @Test
    fun t2() {
        val src = Image.makeFromEncoded(File("F:/D/1.jpg").readBytes())
        val img = src.newSurface()
            .withCanvas { drawImage(src, 0F, 0F, paint) }
            .let { img ->
                if (img.width > limit || img.height > limit) {
                    val size = max(img.width, img.height)
                    val rate = limit.toFloat() / size
                    val w = (img.width * rate).toInt()
                    val h = (img.height * rate).toInt()
                    img.resize(w, h)
                } else img
            }
            .toBitmap()
            .binary()
        println(con(img, 1, 1))
    }

    @Test
    fun t3() {
        val src = runBlocking(Dispatchers.IO) {
            EmbossHandler.handleBytes(File("F:/D/1.jpg").readBytes(), mutableMapOf())
        }.getOrThrow()[0].image
        val img = src.newSurface()
            .withCanvas { drawImage(src, 0F, 0F, paint) }
            .let { img ->
                if (img.width > limit || img.height > limit) {
                    val size = max(img.width, img.height)
                    val rate = limit.toFloat() / size
                    val w = (img.width * rate).toInt()
                    val h = (img.height * rate).toInt()
                    img.resize(w, h)
                } else img
            }
            .toBitmap()
            .binary()
        println(con(img, 1, 1))
    }

    private val s =
        "⡀⠁⠂⠃⠄⠅⠆⠇⠈⠉⠊⠋⠌⠍⠎⠏⠐⠑⠒⠓⠔⠕⠖⠗⠘⠙⠚⠛⠜⠝⠞⠟⠠⠡⠢⠣⠤⠥⠦⠧⠨⠩⠪⠫⠬⠭⠮⠯⠰⠱⠲⠳⠴⠵⠶⠷⠸⠹⠺⠻⠼⠽⠾⠿⡀⡁⡂⡃⡄⡅⡆⡇⡈⡉⡊⡋⡌⡍⡎⡏⡐⡑⡒⡓⡔⡕⡖⡗⡘⡙⡚⡛⡜⡝⡞⡟⡠⡡⡢⡣⡤⡥⡦⡧⡨⡩⡪⡫⡬⡭⡮⡯⡰⡱⡲⡳⡴⡵⡶⡷⡸⡹⡺⡻⡼⡽⡾⡿⢀⢁⢂⢃⢄⢅⢆⢇⢈⢉⢊⢋⢌⢍⢎⢏⢐⢑⢒⢓⢔⢕⢖⢗⢘⢙⢚⢛⢜⢝⢞⢟⢠⢡⢢⢣⢤⢥⢦⢧⢨⢩⢪⢫⢬⢭⢮⢯⢰⢱⢲⢳⢴⢵⢶⢷⢸⢹⢺⢻⢼⢽⢾⢿⣀⣁⣂⣃⣄⣅⣆⣇⣈⣉⣊⣋⣌⣍⣎⣏⣐⣑⣒⣓⣔⣕⣖⣗⣘⣙⣚⣛⣜⣝⣞⣟⣠⣡⣢⣣⣤⣥⣦⣧⣨⣩⣪⣫⣬⣭⣮⣯⣰⣱⣲⣳⣴⣵⣶⣷⣸⣹⣺⣻⣼⣽⣾⣿"

    /**
     * 生成点阵字符画
     *
     * @param src 图片(灰度)
     * @param ud 用于展示的环境的上下间隔宽度
     * @param lr 用于展示的环境的左右间隔宽度
     * @return 字符画
     */
    fun con(src: BitImage, ud: Int, lr: Int): String {
        val sb = StringBuilder()
        for (h in src.yRange step 4 + ud) {
            if (h + 4 > src.h) continue
            for (w in src.xRange step 2 + lr) {
                if (w + 2 > src.w) continue
                fun i(x: Int, y: Int) = src[w + x, h + y]
                var i = 0
                if (i(0, 0)) i = i or 0b00000001
                if (i(0, 1)) i = i or 0b00000010
                if (i(0, 2)) i = i or 0b00000100
                if (i(1, 0)) i = i or 0b00001000
                if (i(1, 1)) i = i or 0b00010000
                if (i(1, 2)) i = i or 0b00100000
                if (i(0, 3)) i = i or 0b01000000
                if (i(1, 3)) i = i or 0b10000000
                sb.append(s[i])
            }
            sb.append("\n")
        }
        return sb.toString()
    }


    /**
     * 将灰度图二值化
     */
    fun Bitmap.binary(): BitImage {
        val threshold = otsuThreshold(this, height, width)
        val rst = BitImage(width, height)
        for (x in 0 until width) for (y in 0 until height) {
            rst[x, y] = getColor(x, y).red() > threshold
        }
        return rst
    }

    class BitImage(val w: Int, val h: Int) {
        val array = Array(w * h) { false }
        inline val xRange get() = 0 until w
        inline val yRange get() = 0 until h

        operator fun get(x: Int, y: Int) = array[y * w + x]
        operator fun set(x: Int, y: Int, value: Boolean) {
            array[y * w + x] = value
        }
    }

    /**
     * 通过OTSU大津算法计算分割阈值
     *
     * @param bitmap 灰度图
     * @return 阈值
     */
    private fun otsuThreshold(bitmap: Bitmap, h: Int, w: Int): Int {
        val grayScale = 256
        // 每个灰度像素的数量
        val pixelCount = IntArray(grayScale)
        // 每个像素点所占的比例
        val pixelPro = FloatArray(grayScale)
        // 像素点的数量
        val pixelSum = h * w
        // 分割的阈值点
        var threshold = 0

        // 统计灰度级中每个像素在整幅图像中的个数
        for (i in 0 until w) for (j in 0 until h) {
            pixelCount[bitmap.getColor(i, j).red()]++
        }

        // 计算每个像素在整幅图像中的比例
        for (i in 0 until grayScale) {
            pixelPro[i] = pixelCount[i].toFloat() / pixelSum
        }

        // 遍历灰度级[0,255]
        // 公式:g = backgroundRatio * pow((backGrayAverage - grayAverage), 2) + prospectRatio * pow((proGrayAverage - grayAverage), 2)

        // 结果最大值
        var deltaMax = 0.0
        for (i in 0 until grayScale) {
            // 初始化
            var u1tmp = 0f
            var u0tmp = 0f
            // 前景像素点占整幅图像的比例
            var prospectRatio = 0f
            // 背景像素点占整幅图像的比例
            var backgroundRatio = 0f
            for (j in 0 until grayScale) if (j <= i) {
                //背景部分
                backgroundRatio += pixelPro[j]
                u0tmp += j * pixelPro[j] // u0tmp=像素的灰度*像素占的比例
            } else {
                //前景部分
                prospectRatio += pixelPro[j]
                u1tmp += j * pixelPro[j]
            }
            // 背景像素的平均灰度
            val backGrayAverage = u0tmp / backgroundRatio
            // 前景像素的平均灰度
            val proGrayAverage = u1tmp / prospectRatio
            // 整幅图像的平均灰度
            val grayAverage = u0tmp + u1tmp
            // 记录中间值
            val deltaTmp = backgroundRatio * (backGrayAverage - grayAverage).toDouble().pow(2.0) + prospectRatio * (proGrayAverage - grayAverage).toDouble().pow(2.0)
            if (deltaTmp > deltaMax) {
                deltaMax = deltaTmp
                threshold = i
            }
        }
        return threshold
    }
}
