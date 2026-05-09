@file:Suppress("UNUSED")

package top.e404.skiko

import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.TypefaceFontProvider
import java.io.File
import java.util.*
import java.awt.Font as AwtFont

enum class FontType(name: String) {
    LW_LIGHT("LXGWWenKai-Light.ttf"),
    LW("LXGWWenKai-Regular.ttf"),
    LW_BOLD("LXGWWenKai-Bold.ttf"),
    DF_LEISHO_SB("DFLeiSho-SB.ttf"),
    YGODIY_MATRIXBOLDSMALLCAPS("YGODIY-MatrixBoldSmallCaps.ttf"),
    YGO_DIY_GB("YGO-DIY-GB.ttf"),
    YGODIY_JP("YGODIY-JP.otf"),
    YGO_DIY_2_BIG5("YGO-DIY-2-BIG5.ttf"),
    FOT_RODIN("FOT-Rodin Pro M.ttf"),
    MONO_LIGHT("Mono-Light.ttf"),
    MONO("Mono-Regular.ttf"),
    MONO_BOLD("Mono-Bold.ttf"),
    YAHEI_LIGHT("msyhl.ttc"),
    YAHEI("msyh.ttc"),
    YAHEI_BOLD("msyhbd.ttc"),
    MI_LIGHT("MiSans-Light.ttf"),
    MI("MiSans-Regular.ttf"),
    MI_BOLD("MiSans-Bold.ttf"),
    HEI("simhei.ttf"),
    MINECRAFT("Minecraft.ttf"),
    ZHONG_SONG("STZHONGS.TTF"),
    GNU_UNIFONT("gnu-unifont-full.ttf"),
    GLOW_SANS("GlowSansSC-Normal-Heavy.otf"),
    RO_G_SAN_SRF_STD("RoGSanSrfStd-Bd.otf"),
    LI_HEI("力黑体.otf");

    private val bytes by lazy { File("${fontDir}/$name").readBytes() }
    val typeface by lazy { FontMgr.default.makeFromData(Data.makeFromBytes(bytes))!! }
    val awtFont by lazy {
        bytes.inputStream().use { AwtFont.createFont(AwtFont.TRUETYPE_FONT, it)!! }
    }

    fun getSkiaFont(size: Float): Font {
        return Font(typeface, size)
    }

    companion object {
        val fontMgr = ServiceLoader.load(TypefaceFontProvider::class.java)
        var fontDir = "data/font"
        fun Font.fullHeight() = metrics.run { descent - ascent }

        init {
            fun File.registerAsFont() {
                if (isDirectory) listFiles()?.forEach { it.registerAsFont() }
                else {
                    fontMgr.forEach {
                        it.registerTypeface(it.makeFromFile(this.path))
                    }
                }
            }
            File(fontDir).registerAsFont()
        }
    }
}
