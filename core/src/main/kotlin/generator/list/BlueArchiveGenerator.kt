package top.e404.skiko.generator.list

import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.ParagraphBuilder
import org.jetbrains.skia.paragraph.ParagraphStyle
import org.jetbrains.skia.paragraph.TextStyle
import org.jetbrains.skia.svg.SVGDOM
import top.e404.skiko.frame.Frame
import top.e404.skiko.generator.ImageGenerator
import top.e404.skiko.util.*
import kotlin.math.max

object BlueArchiveGenerator : ImageGenerator {
    private val families = arrayOf("GlowSansSC-Normal-Heavy.otf", "RoGSanSrfStd-Bd.otf")
    private const val FONT_SIZE = 80F
    private const val FONT_SIZE_BOTTOM = 40F
    private val cross by lazy {
        SVGDOM(Data.makeFromBytes(getJarFile(this.javaClass, "statistic/blue_archive/cross.svg")))
    }
    private val ring by lazy {
        SVGDOM(Data.makeFromBytes(getJarFile(this.javaClass, "statistic/blue_archive/ring.svg")))
    }

    override suspend fun generate(args: MutableMap<String, String>): MutableList<Frame> {
        val l = args["l"] ?: ""
        val r = args["r"] ?: ""
        val b = args["b"] ?: ""
        val r1 = r.substring(0, 1)
        val r2 = r.substring(1)
        // val x = args["x"]?.toFloatOrNull() ?: 0F
        // val y = args["y"]?.toFloatOrNull() ?: 0F
        val lStyle = TextStyle()
            .setFontFamilies(families)
            .setColor(0xff128afa.toInt())
            .setFontStyle(FontStyle(FontWeight.BOLD, FontWidth.NORMAL, FontSlant.OBLIQUE))
            .setFontSize(FONT_SIZE)
            .setBackground(Paint().apply { color = Colors.WHITE.argb })
        val rStyle = TextStyle()
            .setFontFamilies(families)
            .setColor(0xff2b2b2b.toInt())
            .setFontStyle(FontStyle(FontWeight.BOLD, FontWidth.NORMAL, FontSlant.OBLIQUE))
            .setFontSize(FONT_SIZE)
            .setBackground(Paint().apply { color = Colors.WHITE.argb })
        val bStyle = TextStyle()
            .setFontFamilies(families)
            .setColor(0xff2b2b2b.toInt())
            .setFontStyle(FontStyle(FontWeight.BOLD, FontWidth.NORMAL, FontSlant.OBLIQUE))
            .setFontSize(FONT_SIZE_BOTTOM)
        val w = ParagraphBuilder(ParagraphStyle(), Fonts.fonts)
            .pushStyle(lStyle)
            .addText(l)
            .build()
            .layout(10000F)
            .maxIntrinsicWidth
        val w2 = ParagraphBuilder(ParagraphStyle(), Fonts.fonts)
            .pushStyle(lStyle)
            .addText(l)
            .popStyle()
            .pushStyle(rStyle)
            .addText(r1)
            .build()
            .layout(10000F)
            .maxIntrinsicWidth
        val paragraph = ParagraphBuilder(ParagraphStyle(), Fonts.fonts)
            .pushStyle(lStyle)
            .addText(l)
            .popStyle()
            .pushStyle(rStyle)
            .addText(r1)
            .addText("\u2005")
            .addText(r2)
            .build()
            .layout(10000F)
        val paragraphBottom = ParagraphBuilder(ParagraphStyle(), Fonts.fonts)
            .pushStyle(bStyle)
            .addText(b)
            .build()
            .layout(10000F)
        val image = Surface.makeRasterN32Premul(
            (max(paragraph.maxIntrinsicWidth, w + paragraphBottom.maxIntrinsicWidth) + 2 * FONT_SIZE).toInt(),
            (paragraph.height + 320F).toInt()
        ).fill(Colors.WHITE.argb).withCanvas {
            val tdx = w2 - 30
            val tdy = 50F
            save()
            translate(tdx, tdy)
            ring.render(this)
            restore()
            paragraph.paint(this, FONT_SIZE, FONT_SIZE + 80F)
            paragraphBottom.paint(
                this,
                w2 + 80,
                paragraph.height + paragraphBottom.height + 100F
            )
            translate(tdx, tdy)
            cross.render(this)
        }
        return mutableListOf(Frame(0, image))
    }
}