package top.e404.skiko.draw.compose

enum class TextOverflow {
    /**
     * 换行显示，超出宽度时自动换行。
     */
    Wrap,

    /**
     * 省略号显示，超出宽度时在末尾添加省略号。
     */
    Ellipsis
}

object TextDefaults {
    const val OVERFLOW_PLACEHOLDER = "…"
}
