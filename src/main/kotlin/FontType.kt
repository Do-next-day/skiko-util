@file:Suppress("UNUSED")

package top.e404.skiko

import org.jetbrains.skia.Data
import org.jetbrains.skia.Font
import org.jetbrains.skia.FontMgr
import java.io.File
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
        var fontDir = "data/font"
        fun Font.fullHeight() = metrics.run { descent - ascent }
    }
}