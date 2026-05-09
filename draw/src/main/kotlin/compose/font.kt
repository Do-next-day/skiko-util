package top.e404.skiko.draw.compose

import org.jetbrains.skia.Data
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Typeface
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Compose DSL 的字体管理器。
 *
 * Compose 语法层只保存字体名，真实的 Skia `Typeface` 只在本地渲染端解析。
 * 这样远程渲染协议可以传递稳定的字体名，而不是无法序列化的 native 对象。
 */
object ComposeFontManager {
    const val DEFAULT = "default"

    private data class SystemFontRef(
        val familyName: String,
        val style: FontStyle
    )

    private val registeredFonts = ConcurrentHashMap<String, Typeface>()
    private val systemFontAliases = ConcurrentHashMap<String, SystemFontRef>()

    var defaultFamily: String = DEFAULT

    var fallbackTypeface: Typeface = Typeface.makeEmpty()

    fun register(name: String, typeface: Typeface): String {
        requireValidName(name)
        registeredFonts[name] = typeface
        return name
    }

    fun registerFile(name: String, file: File, index: Int = 0): String =
        registerFile(name, file.absolutePath, index)

    fun registerFile(name: String, path: String, index: Int = 0): String {
        requireValidName(name)
        val typeface = FontMgr.default.makeFromFile(path, index)
            ?: error("无法加载字体文件: $path")
        return register(name, typeface)
    }

    fun registerBytes(name: String, bytes: ByteArray, index: Int = 0): String =
        registerData(name, Data.makeFromBytes(bytes), index)

    fun registerData(name: String, data: Data, index: Int = 0): String {
        requireValidName(name)
        val typeface = FontMgr.default.makeFromData(data, index)
            ?: error("无法从字体数据加载字体: $name")
        return register(name, typeface)
    }

    fun registerSystem(name: String, familyName: String = name, style: FontStyle = FontStyle.NORMAL): String {
        requireValidName(name)
        systemFontAliases[name] = SystemFontRef(familyName, style)
        return name
    }

    fun resolve(name: String? = defaultFamily): Typeface {
        val family = name?.takeIf { it.isNotBlank() } ?: defaultFamily
        resolveRegisteredOrSystem(family)?.let { return it }
        if (family != defaultFamily) {
            resolveRegisteredOrSystem(defaultFamily)?.let { return it }
        }
        return fallbackTypeface
    }

    fun systemFamilies(): List<String> =
        (0 until FontMgr.default.familiesCount).map { FontMgr.default.getFamilyName(it) }

    fun clearRegistered() {
        registeredFonts.clear()
        systemFontAliases.clear()
        defaultFamily = DEFAULT
        fallbackTypeface = Typeface.makeEmpty()
    }

    private fun resolveRegisteredOrSystem(name: String): Typeface? {
        registeredFonts[name]?.let { return it }
        systemFontAliases[name]?.let { ref ->
            FontMgr.default.matchFamilyStyle(ref.familyName, ref.style)?.let { return it }
        }
        return FontMgr.default.matchFamilyStyle(name, FontStyle.NORMAL)
    }

    private fun requireValidName(name: String) {
        require(name.isNotBlank()) { "字体名不能为空" }
    }
}
