@file:Suppress("unused")

package top.e404.dbf

import java.io.BufferedReader
import java.io.File

object BdfParser {
    /**
     * 文件开始, `BDF`标准版本号, 如`2.1`
     */
    const val START_FONT = "STARTFONT"

    /**
     * 文件结束
     */
    const val END_FONT = "ENDFONT"

    /**
     * 注释
     */
    const val COMMENT = "COMMENT"

    /**
     * 字体名
     */
    const val FONT = "FONT"

    /**
     * 字形尺寸(以`PT`计) 分辨率x(以`DPI`计) 分辨率y(以`DPI`计)
     */
    const val SIZE = "SIZE"

    /**
     * 字体包围盒宽度 字体包围盒高度 分辨率x 分辨率y, 单位像素
     */
    const val FONT_BOUNDING_BOX = "FONTBOUNDINGBOX"

    /**
     * 整型值, 缺省值`0`, 三种可选值
     *
     * - `0` - 从左向右
     * - `1` - 自上而下
     * - `2` - 兼有
     *
     * 若该参数取1, 则`DWIDTH`和`SWIDTH`两关键字可选。
     */
    const val METRICS_SET = "METRICSSET"

    /**
     * 可选属性
     */
    const val START_PROPERTIES = "STARTPROPERTIES"

    /**
     * 字符数量
     */
    const val CHARS = "CHARS"

    /**
     * 开始字符
     */
    const val START_CHAR = "STARTCHAR"

    /**
     * 开始字符点阵
     */
    const val BITMAP = "BITMAP "

    /**
     * 结束字符
     */
    const val END_CHAR = "ENDCHAR"

    /**
     * 结束可选属性
     */
    const val END_PROPERTIES = "ENDPROPERTIES"

    fun parse(file: File) = file.bufferedReader().use(::parse)

    fun parse(text: String) = text.reader().buffered().use(::parse)

    fun parse(reader: BufferedReader): BdfFont {
        val header = parseHeader(reader)
        val map = HashMap<Int, BdfChar>(header.count)
        repeat(header.count) {
            val bdfChar = parseFont(reader)
            map[bdfChar.encoding] = bdfChar
        }
        return BdfFont(header, map)
    }

    private fun parseHeader(reader: BufferedReader): BdfHeader {
        val version = reader.readLine().removePrefix(START_FONT).trim()
        val map = mutableMapOf<String, String>()
        var properties = mutableMapOf<String, String>()
        val count: Int
        while (true) {
            var line = reader.readLine()
            while (line.startsWith(COMMENT)) line = reader.readLine()
            // properties
            if (line.startsWith(START_PROPERTIES, true)) {
                val size = line.substringAfterLast(" ").toInt()
                properties = HashMap(size)
                repeat(size) {
                    var property = reader.readLine()
                    while (property.startsWith(COMMENT)) property = reader.readLine()
                    val index = property.indexOf(" ")
                    require(index != -1)
                    properties[property.substring(0, index)] = property.substring(index + 1)
                }
                require(reader.readLine() == END_PROPERTIES)
            }
            val index = line.indexOf(' ')
            require(index != -1)
            val key = line.substring(0, index)
            if (key == CHARS) {
                count = line.substring(index + 1).toInt()
                break
            }
            map[key] = line.substring(index + 1)
        }
        return BdfHeader(
            version = version,
            font = map[FONT]!!,
            size = BdfSize(map[SIZE]!!),
            boundingBox = FontBoundingBox(map[FONT_BOUNDING_BOX]!!),
            properties = properties,
            count = count
        )
    }

    private fun parseFont(reader: BufferedReader): BdfChar {
        val map = mutableMapOf<String, String>()
        val split = reader.readLine().split(" ")
        require(split.size == 2)
        require(split[0] == START_CHAR)
        val unicode = split[1]
        var line: String
        while (true) {
            line = reader.readLine()
            if (line == BITMAP) break
            val index = line.indexOf(' ')
            require(index != -1) { "unexpected line: $line, unicode: $unicode" }
            val key = line.substring(0, index)
            map[key] = line.substring(index + 1)
        }
        val bbx = FontBoundingBox(map["BBX"]!!)
        // bitmap
        val lines = (1..bbx.h).map {
            reader.readLine()
        }
        line = reader.readLine()
        require(line == END_CHAR) { "unexpected line: $line, unicode: $unicode" }
        return BdfChar(
            unicode,
            map["ENCODING"]!!.toInt(),
            map["SWIDTH"]!!.split(" ").let { it[0].toInt() to it[1].toInt() },
            map["DWIDTH"]!!.split(" ").let { it[0].toInt() to it[1].toInt() },
            bbx,
            BitMatrix(lines)
        )
    }
}

data class BdfFont(
    val header: BdfHeader,
    val chars: Map<Int, BdfChar>
) {
    private companion object {
        fun String.toUnicode(ascii: Boolean) = buildString {
            this@toUnicode.forEach { append(it.toUnicode(ascii)) }
        }

        fun Char.toUnicode(ascii: Boolean): String {
            if (code in 0..127 && !ascii) return toString()
            return "\\u${Integer.toHexString(code).padStart(4, '0')}"
        }
    }

    fun getBitmap(string: String) = chars[string[0].code]
    fun getBitmaps(string: String) = string.toCharArray().map { chars[it.code] }
}

data class BdfHeader(
    val version: String,
    val font: String,
    val size: BdfSize,
    val boundingBox: FontBoundingBox,
    val properties: Map<String, String>,
    val count: Int,
)

/**
 * 尺寸数据
 *
 * @property size 字形尺寸(以PT计)
 * @property x 分辨率x(以DPI计)
 * @property y 分辨率y(以DPI计)
 */
data class BdfSize(
    val size: Int,
    val x: Int,
    val y: Int
) {
    companion object {
        operator fun invoke(string: String): BdfSize {
            val split = string.split(" ")
            require(split.size == 3)
            return BdfSize(split[0].toInt(), split[1].toInt(), split[2].toInt())
        }
    }
}

/**
 * 字体包围盒宽度 字体包围盒高度 分辨率x 分辨率y, 单位像素
 *
 * @property w 字体边界宽度
 * @property h 字体边界高度
 * @property x 字体边界x
 * @property y 字体边界y
 */
data class FontBoundingBox(
    val w: Int,
    val h: Int,
    val x: Int,
    val y: Int,
) {
    companion object {
        operator fun invoke(string: String): FontBoundingBox {
            val split = string.split(" ")
            require(split.size == 4)
            return FontBoundingBox(split[0].toInt(), split[1].toInt(), split[2].toInt(), split[3].toInt())
        }
    }
}

class BdfChar(
    val unicode: String,
    val encoding: Int,
    val sWidth: Pair<Int, Int>,
    val dWidth: Pair<Int, Int>,
    val bbx: FontBoundingBox,
    val bitMatrix: BitMatrix
)
