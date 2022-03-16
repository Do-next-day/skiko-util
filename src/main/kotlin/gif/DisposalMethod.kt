// 参考自 Mirai Skija Plugin by cssxsh https://github.com/cssxsh/mirai-skija-plugin
@file:Suppress("UNUSED")

package top.e404.skiko.gif

/**
 * 处置方法(Disposal Method) 处置图形的方法
 *
 * @property flag Int
 */
enum class DisposalMethod(val flag: Int) {
    /**
     * 不使用处置方法
     */
    UNSPECIFIED(0x00),

    /**
     * 处置图形，把图形从当前位置移去
     */
    DO_NOT_DISPOSE(0x04),

    /**
     * 回复到背景色
     */
    RESTORE_TO_BACKGROUND(0x08),

    /**
     * 回复到先前状态
     */
    RESTORE_TO_PREVIOUS(0x0C),
}