@file:JvmName("ToImage")

package top.e404.skiko.util

import org.jetbrains.skia.Font
import top.e404.skiko.draw.element.Text
import top.e404.skiko.draw.toImage

@Suppress("UNUSED")
fun String.toImage(
    maxWidth: Int = 500,
    udPadding: Int = 3,
    color: Int = Colors.WHITE.argb,
    bgColor: Int = Colors.BG.argb,
    font: Font = defaultFont
) = listOf(
    Text(
        content = this,
        font = font,
        udPadding = udPadding,
        color = color,
        center = false
    )
).toImage(
    imagePadding = 20,
    bgColor = bgColor,
    minWidth = maxWidth,
    radius = 15F
)
