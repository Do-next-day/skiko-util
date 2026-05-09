package top.e404.skiko.util

import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyleSet
import org.jetbrains.skia.Typeface
import org.jetbrains.skia.paragraph.FontCollection
import org.jetbrains.skia.paragraph.TypefaceFontProvider
import java.awt.GraphicsEnvironment

object Fonts {
    val fontMgr = FontMgr.default
    val fontProvider = TypefaceFontProvider()
    val fonts = FontCollection()
        .setDynamicFontManager(fontProvider)
        .setDefaultFontManager(fontMgr)

    val defaultFont: Typeface

    init {
        val name = GraphicsEnvironment.getLocalGraphicsEnvironment().availableFontFamilyNames.first()
        defaultFont = matchFamily(name).getTypeface(0)!!
    }

    fun matchFamily(familyName: String): FontStyleSet {
        val fa = fontProvider.matchFamily(familyName)
        if (fa.count() != 0) return fa
        return fontMgr.matchFamily(familyName)
    }
}