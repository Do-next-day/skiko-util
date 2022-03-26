@file:Suppress("UNUSED")

package top.e404.skiko.draw.element

import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import top.e404.skiko.draw.DrawElement
import top.e404.skiko.draw.Pointer
import top.e404.skiko.util.drawImageRRect
import top.e404.skiko.util.resize

/**
 * 代表一个图片元素
 *
 * @property pic 图片
 * @property left 左侧边距
 * @property center 是否居中(居中时忽略左边距)
 * @property radius 圆角幅度
 * @property top 上边距
 * @property bottom 下边距
 */
class Pic(
    var pic: Image,
    var left: Int,
    var center: Boolean,
    var radius: Float,
    var top: Int,
    var bottom: Int,
) : DrawElement {
    var picW = pic.width
    var picH = pic.height
    override fun size(minWidth: Int, maxWidth: Int): Pair<Int, Int> {
        val maxW = if (!center) maxWidth - left
        else maxWidth
        // 尺寸检查
        if (pic.width > maxW) {
            picW = maxW
            picH = pic.height * picW / pic.width
            pic = pic.resize(picW, picH)
        }
        return Pair(picW + if (!center) left else 0, picH + top + bottom)
    }

    override fun drawToBoard(
        canvas: Canvas,
        pointer: Pointer,
        paint: Paint,
        width: Int,
        imagePadding: Int,
    ) {
        val left = if (center) (width + 2 * imagePadding - picW) / 2F
        else pointer.x + left.toFloat()
        val t = pointer.y + top.toFloat()
        canvas.drawImageRRect(pic, left, t, pic.width.toFloat(), pic.height.toFloat(), radius)

        //canvas.drawRoundCorneredImage(
        //    pic,
        //    if (center) (width + 2 * imagePadding - picW) / 2F else pointer.x + left.toFloat(),
        //    pointer.y + top.toFloat(),
        //    radius
        //)
        pointer.y += picH + top + bottom
    }
}