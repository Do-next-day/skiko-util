package top.e404.skiko.generator.list

/*
object CodeGenerator : ImageHandler {
    private const val padding = 50
    private const val fontSize = 150
    private const val fontSpacing = 30
    private val font = FontType.MINECRAFT.getSkijaFont(fontSize.toFloat())
    private val chars = "qwertyuipadfghjkzxcvbnmWERTYUIPADFGHJKLZXCVBNM23478".toCharArray().toList()

    fun genCodeText(length: Int = 4): String {
        require(length > 0) { "length must > 0" }
        return buildString {
            repeat(length) { append(chars.choose()) }
        }
    }

    fun chooseColor(length: Int = 4): List<Int> {
        val colors = Colors.values()
        var tmp = colors.toMutableList()
        return buildList {
            repeat(length) {
                if (tmp.isEmpty()) tmp = colors.toMutableList()
                add(tmp.takeRandom().value)
            }
        }
    }

    override suspend fun handleFrame(
        index: Int,
        count: Int,
        image: Image,
        data: HandlerData?,
        frame: Frame,
    ): Image {
        val (text, colors) = data as CodeInfo
        val width = text.length * (fontSize + fontSpacing) - fontSpacing + 2 * padding
        val height = fontSize + 2 * padding
        return Surface.makeRasterN32Premul(width, height).run {
            val paint = Paint()
            canvas.apply {
                for ((i, c) in text.withIndex()) {
                    save()
                    val dx = padding + i * (fontSize + fontSpacing) - fontSpacing
                    val dy = padding + fontSize
                    translate(dx.toFloat(), dy.toFloat())

                    //font.metrics.
                    val line = TextLine.make(c.toString(), font)
                    drawTextLine(line, 0F, 0F, paint.apply { color = colors[i] })
                    restore()
                }
            }
            makeImageSnapshot()
        }
    }

    data class CodeInfo(
        val text: String,
        val colors: List<Int>
    ) : HandlerData
}*/
