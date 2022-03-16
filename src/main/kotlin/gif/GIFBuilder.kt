// 参考自 Mirai Skija Plugin by cssxsh https://github.com/cssxsh/mirai-skija-plugin
@file:Suppress("UNUSED")

package top.e404.skiko.gif

import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Data
import org.jetbrains.skia.IRect
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * 构建gif
 *
 * @property width 宽度
 * @property height 高度
 */
class GIFBuilder(val width: Int, val height: Int) {
    companion object {
        internal val GIF_HEADER = "GIF89a".toByteArray(Charsets.US_ASCII)
        internal val GIF_TRAILER = ";".toByteArray(Charsets.US_ASCII)
        internal fun header(buffer: ByteBuffer) = buffer.put(GIF_HEADER)
        internal fun trailer(buffer: ByteBuffer) = buffer.put(GIF_TRAILER)
    }

    internal var capacity = 1 shl 23

    /**
     * 设置容量
     *
     * [ByteBuffer.capacity]
     */
    fun capacity(total: Int) =
        apply { capacity = total }

    internal var loop = 0

    /**
     * gif循环播放
     *
     * Netscape Looping Application Extension, 0 is infinite times
     * @see [ApplicationExtension.loop]
     */
    fun loop(count: Int) =
        apply { loop = count }

    internal var buffering = 0

    /**
     * Netscape Buffering Application Extension
     * @see [ApplicationExtension.buffering]
     */
    fun buffering(open: Boolean) =
        apply {
            buffering = if (open) 0x0001_0000
            else 0x0000_0000
        }

    internal var ratio: Int = 0

    /**
     * 像素纵横比
     *
     * Pixel Aspect Ratio
     * @see [LogicalScreenDescriptor.write]
     */
    fun ratio(size: Int) =
        apply { ratio = size }

    internal var options = FrameOptions(
        method = DisposalMethod.UNSPECIFIED,
        input = false,
        transparency = false,
        duration = 100,
        table = ColorTable.Empty,
        rect = IRect.makeLTRB(0, 0, 0, 0)
    )

    /**
     * 全局属性设置
     */
    fun options(block: FrameOptions.() -> Unit) =
        apply { options.apply(block) }

    /**
     * 全局色表
     * @see [OctTreeQuantizer.quantize]
     */
    fun table(bitmap: Bitmap) =
        apply {
            options.table = ColorTable(OctTreeQuantizer.quantize(bitmap, 256))
        }

    /**
     * 全局色表
     */
    fun table(value: ColorTable) = apply {
        options.table = value
    }

    internal var frames: MutableList<Pair<Bitmap, FrameOptions>> = ArrayList()

    /**
     * 添加一帧
     *
     * @param bitmap 图像
     * @param block 设置
     * @return GIFBuilder
     */
    fun frame(bitmap: Bitmap, block: FrameOptions.() -> Unit = {}) =
        apply {
            val rect = IRect.makeLTRB(0, 0, bitmap.width, bitmap.height)
            frames.add(bitmap to options.copy(table = this.options.table, rect = rect).apply(block))
        }

    /**
     * 构建gif
     *
     * @param buffer ByteBuffer
     */
    fun build(buffer: ByteBuffer) {
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        header(buffer)
        LogicalScreenDescriptor.write(buffer, width, height, options.table, ratio)
        if (loop >= 0) ApplicationExtension.loop(buffer, loop)
        if (buffering > 0) ApplicationExtension.buffering(buffer, buffering)
        for ((bitmap, options) in frames) {
            val table = when {
                options.table.exists() -> options.table
                this.options.table.exists() -> this.options.table
                else -> ColorTable(OctTreeQuantizer.quantize(bitmap, 256))
            }
            val index = if (options.transparency) table.background else null

            GraphicControlExtension.write(buffer, options.method, options.input, index, options.duration)

            val result = AtkinsonDitherer.dither(bitmap, table.colors)

            ImageDescriptor.write(buffer, options.rect, table, table !== this.options.table, result)
        }
        trailer(buffer)
    }

    fun data(): Data {
        val data = Data.makeFromBytes(ByteArray(capacity))
        val buffer = ByteBuffer.wrap(data.bytes)
        build(buffer = buffer)
        return data.makeSubset(0, buffer.position())
    }

    fun build(): ByteArray {
        val buffer = ByteBuffer.allocate(capacity)
        build(buffer = buffer)

        return ByteArray(buffer.position()).also { buffer.get(it) }
    }

    /**
     * 代表一帧的设置
     *
     * @property method 处置方法 @see [DisposalMethod]
     * @property input 用户输入标志(Use Input Flag) 指出是否期待用户有输入之后才继续进行下去
     * @property transparency 是否带透明度
     * @property duration 一帧的持续时长, 单位毫秒
     * @property table 色表
     * @property rect 矩形
     */
    data class FrameOptions(
        var method: DisposalMethod,
        var input: Boolean,
        var transparency: Boolean,
        var duration: Int,
        var table: ColorTable,
        var rect: IRect,
    )
}