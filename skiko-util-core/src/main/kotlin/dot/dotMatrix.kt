package top.e404.skiko.dot

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import top.e404.dbf.BdfFont
import top.e404.dbf.BitMatrix
import top.e404.dbf.plus
import top.e404.skiko.util.*
import kotlin.math.max
import kotlin.math.pow

private const val limit = 220
private val paint = Paint().apply { colorFilter = grayMatrix }

/**
 * 字符序列
 */
private const val sequence =
    "⡀⠁⠂⠃⠄⠅⠆⠇⠈⠉⠊⠋⠌⠍⠎⠏⠐⠑⠒⠓⠔⠕⠖⠗⠘⠙⠚⠛⠜⠝⠞⠟⠠⠡⠢⠣⠤⠥⠦⠧⠨⠩⠪⠫⠬⠭⠮⠯⠰⠱⠲⠳⠴⠵⠶⠷⠸⠹⠺⠻⠼⠽⠾⠿⡀⡁⡂⡃⡄⡅⡆⡇⡈⡉⡊⡋⡌⡍⡎⡏⡐⡑⡒⡓⡔⡕⡖⡗⡘⡙⡚⡛⡜⡝⡞⡟⡠⡡⡢⡣⡤⡥⡦⡧⡨⡩⡪⡫⡬⡭⡮⡯⡰⡱⡲⡳⡴⡵⡶⡷⡸⡹⡺⡻⡼⡽⡾⡿⢀⢁⢂⢃⢄⢅⢆⢇⢈⢉⢊⢋⢌⢍⢎⢏⢐⢑⢒⢓⢔⢕⢖⢗⢘⢙⢚⢛⢜⢝⢞⢟⢠⢡⢢⢣⢤⢥⢦⢧⢨⢩⢪⢫⢬⢭⢮⢯⢰⢱⢲⢳⢴⢵⢶⢷⢸⢹⢺⢻⢼⢽⢾⢿⣀⣁⣂⣃⣄⣅⣆⣇⣈⣉⣊⣋⣌⣍⣎⣏⣐⣑⣒⣓⣔⣕⣖⣗⣘⣙⣚⣛⣜⣝⣞⣟⣠⣡⣢⣣⣤⣥⣦⣧⣨⣩⣪⣫⣬⣭⣮⣯⣰⣱⣲⣳⣴⣵⣶⣷⣸⣹⣺⣻⣼⣽⣾⣿"

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

/**
 * 将图片转为灰度的bitmap
 *
 * @param src 原图
 */
fun gray(src: Image) = src.newSurface()
    .withCanvas { drawImage(src, 0F, 0F, paint) }
    .let { img ->
        if (img.width > limit || img.height > limit) {
            val size = max(img.width, img.height)
            val rate = limit.toFloat() / size
            val w = (img.width * rate).toInt()
            val h = (img.height * rate).toInt()
            img.resize(w, h)
        } else img
    }.toBitmap()

/**
 * 将图片转为灰度图并且二值化
 *
 * @param image 图片
 * @return 点阵图
 */
fun binary(image: Image) = binary(gray(image))

/**
 * 将灰度图二值化
 *
 * @param bitmap 灰度图
 * @return 点阵图
 */
fun binary(bitmap: Bitmap): BitMatrix {
    val threshold = otsuThreshold(bitmap, bitmap.height, bitmap.width)
    val rst = BitMatrix(bitmap.width, bitmap.height)
    for (x in 0 until bitmap.width) for (y in 0 until bitmap.height) {
        rst[x, y] = bitmap.getColor(x, y).red() > threshold
    }
    return rst
}


/**
 * 生成点阵字符画
 *
 * @param ud 用于展示的环境的上下间隔宽度
 * @param lr 用于展示的环境的左右间隔宽度
 * @return 字符画
 */
fun BitMatrix.generator(ud: Int, lr: Int): String {
    val sb = StringBuilder()
    for (h in yRange step 4 + ud) {
        if (h + 4 > height) continue
        for (w in xRange step 2 + lr) {
            if (w + 2 > width) continue
            fun i(x: Int, y: Int) = get(w + x, h + y)
            var i = 0
            if (i(0, 0)) i = i or 0b00000001
            if (i(0, 1)) i = i or 0b00000010
            if (i(0, 2)) i = i or 0b00000100
            if (i(1, 0)) i = i or 0b00001000
            if (i(1, 1)) i = i or 0b00010000
            if (i(1, 2)) i = i or 0b00100000
            if (i(0, 3)) i = i or 0b01000000
            if (i(1, 3)) i = i or 0b10000000
            sb.append(sequence[i])
        }
        sb.append("\n")
    }
    return sb.toString().removeSuffix("\n")
}

fun String.toBitMatrix(font: BdfFont): BitMatrix {
    val list = font.getBitmaps(this).filterNotNull().toMutableList()
    var matrix = list.removeFirst().bitMatrix
    while (list.isNotEmpty()) matrix += list.removeFirst().bitMatrix
    return matrix
}
