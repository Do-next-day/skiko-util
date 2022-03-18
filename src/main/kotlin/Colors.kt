@file:Suppress("UNUSED")

package top.e404.skiko

enum class Colors(val value: Int) {
    BG(0xFF1F1B1D.toInt()),

    WHITE(0xFFFFFFFF.toInt()),
    LIGHT_GRAY(0xFFC0C0C0.toInt()),
    GRAY(0xFF808080.toInt()),
    DARK_GRAY(0xFF404040.toInt()),
    BLACK(0xFF000000.toInt()),

    PINK(0xFFFFAFAF.toInt()),
    RED(0xFFFF0000.toInt()),
    ORANGE(0xFFFF7700.toInt()),
    YELLOW(0xFFFFFF00.toInt()),
    YELLOW_GREEN(0xFF77FF00.toInt()),
    GREEN(0xFF00FF00.toInt()),
    BLUE_GREEN(0xFF00FF77.toInt()),
    CYAN(0xFF00FFFF.toInt()),
    LIGHT_BLUE(0xFF0077FF.toInt()),
    BLUE(0xFF00FFFF.toInt()),
    PURPLE(0xFF7700FF.toInt()),
    MAGENTA(0xFFFF00FF.toInt()),
    PURPLE_RED(0xFFFF0077.toInt());
}