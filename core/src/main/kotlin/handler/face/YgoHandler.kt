package top.e404.skiko.handler.face

import org.jetbrains.skia.*
import org.jetbrains.skia.paragraph.*
import top.e404.skiko.util.Colors
import top.e404.skiko.FontType
import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.handle
import top.e404.skiko.util.getJarImage
import top.e404.skiko.util.newSurface
import top.e404.skiko.util.subCenter
import top.e404.skiko.util.withCanvas

/**
 * 球拍
 */
@ImageHandler
object YgoHandler : FramesHandler {
    override val name = "Ygo"
    override val regex = Regex("(?i)ygo")

    // 卡片背景
    private val monsterRh by lazy { getJarImage(this::class.java, "statistic/ygo/card/monster_rh.jpg") }
    private val monsterTc by lazy { getJarImage(this::class.java, "statistic/ygo/card/monster_tc.jpg") }
    private val monsterTt by lazy { getJarImage(this::class.java, "statistic/ygo/card/monster_tt.jpg") }
    private val monsterXg by lazy { getJarImage(this::class.java, "statistic/ygo/card/monster_xg.jpg") }
    private val monsterYs by lazy { getJarImage(this::class.java, "statistic/ygo/card/monster_ys.jpg") }
    private val spell by lazy { getJarImage(this::class.java, "statistic/ygo/card/spell.jpg") }
    private val trap by lazy { getJarImage(this::class.java, "statistic/ygo/card/trap.jpg") }

    // 属性
    private val attrDivine by lazy { getJarImage(this::class.java, "statistic/ygo/attr/divine.png") }
    private val attrLight by lazy { getJarImage(this::class.java, "statistic/ygo/attr/light.png") }
    private val attrDark by lazy { getJarImage(this::class.java, "statistic/ygo/attr/dark.png") }
    private val attrEarth by lazy { getJarImage(this::class.java, "statistic/ygo/attr/earth.png") }
    private val attrFire by lazy { getJarImage(this::class.java, "statistic/ygo/attr/fire.png") }
    private val attrWind by lazy { getJarImage(this::class.java, "statistic/ygo/attr/wind.png") }
    private val attrWater by lazy { getJarImage(this::class.java, "statistic/ygo/attr/water.png") }

    private val attrSpell by lazy { getJarImage(this::class.java, "statistic/ygo/attr/spell.png") }
    private val attrTrap by lazy { getJarImage(this::class.java, "statistic/ygo/attr/trap.png") }

    private val monsterAttrAllow = mapOf(
        "神" to { attrDivine },
        "光" to { attrLight },
        "暗" to { attrDark },
        "地" to { attrEarth },
        "火" to { attrFire },
        "风" to { attrWind },
        "水" to { attrWater },
    )

    private val spellTypeAllow = mapOf(
        "通常" to { null },
        "场地" to { iconCd },
        "速攻" to { iconSg },
        "装备" to { iconZb },
        "永续" to { iconYx },
        "仪式" to { iconYs },
    )

    private val trapTypeAllow = mapOf(
        "通常" to { null },
        "反击" to { iconFj },
        "永续" to { iconYx },
    )

    // 特殊 魔法陷阱卡
    private val iconCd by lazy { getJarImage(this::class.java, "statistic/ygo/icon/cd.png") }
    private val iconFj by lazy { getJarImage(this::class.java, "statistic/ygo/icon/fj.png") }
    private val iconSg by lazy { getJarImage(this::class.java, "statistic/ygo/icon/sg.png") }
    private val iconYs by lazy { getJarImage(this::class.java, "statistic/ygo/icon/ys.png") }
    private val iconYx by lazy { getJarImage(this::class.java, "statistic/ygo/icon/yx.png") }
    private val iconZb by lazy { getJarImage(this::class.java, "statistic/ygo/icon/zb.png") }

    // 其他
    private val logo by lazy { getJarImage(this::class.java, "statistic/ygo/logo.png") }
    private val level by lazy { getJarImage(this::class.java, "statistic/ygo/level.png") }

    // 字体
    private val fontAttr = FontType.YGODIY_MATRIXBOLDSMALLCAPS.getSkiaFont(35F)
    private val fontId = FontType.YGODIY_MATRIXBOLDSMALLCAPS.getSkiaFont(25F)
    private val fontCopyright = FontType.FOT_RODIN.getSkiaFont(20F)

    // paint
    private val paint = Paint().apply { color = Colors.BLACK.argb }

    // rect
    private val rectImage = Rect.makeXYWH(101F, 220F, 612F, 612F)
    private val rectAttr = Rect.makeXYWH(680F, 58F, 73F, 73F)
    private val rectType = Rect.makeXYWH(667F, 148F, 46F, 46F)
    private val rectLine = Rect.makeXYWH(64F, 1078F, 683F, 2F)
    private val rectLogo = Rect.makeXYWH(743F, 1115F, 43F, 43F)


    private val mgr = TypefaceFontProvider()
        .registerTypeface(FontType.DF_LEISHO_SB.typeface, "YGO-1")
        .registerTypeface(FontType.YGO_DIY_2_BIG5.typeface, "YGO-2")
        .registerTypeface(FontType.YGO_DIY_GB.typeface, "YGO-3")
        .registerTypeface(FontType.YGODIY_JP.typeface, "YGO-4")
        .registerTypeface(FontType.YGODIY_MATRIXBOLDSMALLCAPS.typeface, "YGO-5")
    private val fonts = FontCollection()
        .setDynamicFontManager(mgr)
        .setDefaultFontManager(FontMgr.default)

    // context
    private val families = arrayOf("YGO-1", "YGO-2", "YGO-3", "YGO-4", "YGO-5")

    private val contextTitle = ParagraphStyle().apply {
        maxLinesCount = 1
        textStyle = TextStyle()
            .setFontSize(24F)
            .setColor(Color.BLACK)
            .setFontFamilies(families)
    }
    private val contextDesc = ParagraphStyle().apply {
        maxLinesCount = 7
        textStyle = TextStyle()
            .setFontSize(24F)
            .setColor(Color.BLACK)
            .setFontFamilies(families)
    }
    private val contextNameBlack = ParagraphStyle().apply {
        maxLinesCount = 1
        textStyle = TextStyle()
            .setFontSize(60F)
            .setColor(Color.BLACK)
            .setFontFamilies(families)
    }
    private val contextNameWhite = ParagraphStyle().apply {
        maxLinesCount = 1
        textStyle = TextStyle()
            .setFontSize(60F)
            .setColor(Color.WHITE)
            .setFontFamilies(families)
    }
    private val contextType = ParagraphStyle().apply {
        maxLinesCount = 1
        textStyle = TextStyle()
            .setFontSize(45F)
            .setColor(Color.BLACK)
            .setFontFamilies(families)
    }

    private enum class Category(
        val alias: String,
        val defaultType: String,
        val types: List<String>,
        val draw: (
            img: Image,
            name: String?,
            desc: String?,
            type: String,
            args: MutableMap<String, String>
        ) -> Image
    ) {
        MONSTER("怪兽", "通常", listOf("通常", "效果", "融合", "仪式", "同调"), fun(
            img: Image,
            name: String?,
            desc: String?,
            type: String?,
            args: MutableMap<String, String>
        ): Image {
            val card = when (type) {
                "效果" -> monsterXg
                "融合" -> monsterRh
                "仪式" -> monsterYs
                "同调" -> monsterTt
                else -> monsterTc
            }
            return card.newSurface().withCanvas {
                drawImage(card, 0F, 0F)
                drawImageRect(img, rectImage)
                // name
                ParagraphBuilder(contextNameBlack, fonts)
                    .addText(name ?: "")
                    .build()
                    .layout(600F)
                    .paint(this, 72F, 66F)
                // title
                ParagraphBuilder(contextTitle, fonts)
                    .addText(args["title"]?.let { "【$it】" } ?: "【未知/通常】")
                    .build()
                    .layout(690F)
                    .paint(this, 50F, 896F)
                // desc
                ParagraphBuilder(contextDesc, fonts)
                    .addText(desc ?: "")
                    .build()
                    .layout(690F)
                    .paint(this, 63F, 922F)
                // attr
                val attrImage = monsterAttrAllow[args["attr"]]?.invoke() ?: attrLight
                drawImageRect(attrImage, rectAttr)
                // lv
                repeat((args["lv"]?.toIntOrNull() ?: 4).coerceIn(0, 12)) {
                    drawImageRect(level, Rect.makeXYWH(740F - (it + 1) * 49 - it * 7, 146F, 49F, 49F))
                }
                // line
                drawRect(rectLine, paint)
                val atk = TextLine.make("ATK/${args["atk"] ?: 1000}", fontAttr)
                val def = TextLine.make("DEF/${args["def"] ?: 1000}", fontAttr)
                drawTextLine(atk, 443F, 1107F, paint)
                drawTextLine(def, 612F, 1107F, paint)
                // id
                args["id"]?.let { drawTextLine(TextLine.make(it, fontId), 42F, 1147F, paint) }
                // copyright
                val copyright = TextLine.make(args["copyright"] ?: "ⓒスタジオ·ダイス /集英社·テレビ東京·KONAMI", fontCopyright)
                drawTextLine(copyright, 730F - copyright.width, 1147F, paint)
                drawImageRect(logo, rectLogo)
            }
        }),
        SPELL("魔法", "通常", listOf("通常", "场地", "速攻", "永续", "仪式", "装备"), fun(
            img: Image,
            name: String?,
            desc: String?,
            type: String?,
            args: MutableMap<String, String>
        ) = spell.newSurface().withCanvas {
            drawImage(spell, 0F, 0F)
            drawImageRect(img, rectImage)
            // name
            ParagraphBuilder(contextNameWhite, fonts)
                .addText(name ?: "")
                .build()
                .layout(600F)
                .paint(this, 74F, 66F)
            // desc
            ParagraphBuilder(contextDesc, fonts)
                .addText(desc ?: "")
                .build()
                .layout(690F)
                .paint(this, 63F, 896F)
            // type
            val typeImage = type?.let { spellTypeAllow[it] }?.invoke()
            if (typeImage == null) ParagraphBuilder(contextType, fonts)
                .addText("【魔法卡】")
                .build()
                .layout(690F)
                .paint(this, 533F, 148F)
            else {
                ParagraphBuilder(contextType, fonts)
                    .addText("【魔法卡")
                    .build()
                    .layout(690F)
                    .paint(this, 484F, 148F)
                drawImageRect(typeImage, rectType)
                ParagraphBuilder(contextType, fonts)
                    .addText("】")
                    .build()
                    .layout(690F)
                    .paint(this, 716F, 148F)
            }
            // attr
            drawImageRect(attrSpell, rectAttr)
            // id
            args["id"]?.let { drawTextLine(TextLine.make(it, fontId), 42F, 1147F, paint) }
            // copyright
            val copyright = TextLine.make(args["copyright"] ?: "ⓒスタジオ·ダイス /集英社·テレビ東京·KONAMI", fontCopyright)
            drawTextLine(copyright, 730F - copyright.width, 1147F, paint)
            drawImageRect(logo, rectLogo)
        }),
        TRAP("陷阱", "通常", listOf("通常", "反击", "永续"), fun(
            img: Image,
            name: String?,
            desc: String?,
            type: String?,
            args: MutableMap<String, String>
        ) = trap.newSurface().withCanvas {
            drawImage(trap, 0F, 0F)
            drawImageRect(img, rectImage)
            // name
            ParagraphBuilder(contextNameWhite, fonts)
                .addText(name ?: "")
                .build()
                .layout(600F)
                .paint(this, 74F, 66F)
            // desc
            ParagraphBuilder(contextDesc, fonts)
                .addText(desc ?: "")
                .build()
                .layout(690F)
                .paint(this, 63F, 896F)
            // type
            val typeImage = type?.let { trapTypeAllow[it] }?.invoke()
            if (typeImage == null) ParagraphBuilder(contextType, fonts)
                .addText("【陷阱卡】")
                .build()
                .layout(690F)
                .paint(this, 533F, 148F)
            else {
                ParagraphBuilder(contextType, fonts)
                    .addText("【陷阱卡")
                    .build()
                    .layout(690F)
                    .paint(this, 484F, 148F)
                drawImageRect(typeImage, rectType)
                ParagraphBuilder(contextType, fonts)
                    .addText("】")
                    .build()
                    .layout(690F)
                    .paint(this, 716F, 148F)
            }
            // attr
            drawImageRect(attrTrap, rectAttr)
            // id
            args["id"]?.let { drawTextLine(TextLine.make(it, fontId), 42F, 1147F, paint) }
            // copyright
            val copyright = TextLine.make(args["copyright"] ?: "ⓒスタジオ·ダイス /集英社·テレビ東京·KONAMI", fontCopyright)
            drawTextLine(copyright, 730F - copyright.width, 1147F, paint)
            drawImageRect(logo, rectLogo)
        });
    }


    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ): HandleResult {
        val c = Category.values().firstOrNull { it.alias == args["c"] } ?: Category.MONSTER
        val t = args["t"].let { if (it == null || it !in c.types) c.defaultType else it }
        return frames.result {
            handle { c.draw.invoke(it.subCenter(), args["name"], args["desc"], t, args) }
        }
    }
}
