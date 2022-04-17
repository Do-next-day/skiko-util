@file:Suppress("UNUSED")

package top.e404.skiko

import org.jetbrains.skia.Data
import org.jetbrains.skia.Font
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.Typeface
import top.e404.skiko.util.getJarFile

enum class FontType(name: String) {
    LW_LIGHT("LXGWWenKai-Light.ttf"),
    LW("LXGWWenKai-Regular.ttf"),
    LW_BOLD("LXGWWenKai-Bold.ttf"),
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

    val bytes = lazy { getJarFile("font/$name") }

    fun getSkijaTypeface(): Typeface {
        return FontMgr.default.makeFromData(Data.makeFromBytes(bytes.value))!!
    }

    fun getSkijaFont(size: Float): Font {
        return Font(getSkijaTypeface(), size)
    }
}