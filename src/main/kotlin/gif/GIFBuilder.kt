package top.e404.skiko.gif

import org.jetbrains.skia.*
import org.jetbrains.skia.impl.BufferUtil
import top.e404.skiko.alpha
import top.e404.skiko.util.any
import java.nio.ByteBuffer
import java.nio.ByteOrder

class GIFBuilder(val width: Int, val height: Int) {
    companion object {
        /**
         * GIF标识头
         */
        internal val GIF_HEADER = "GIF89a".toByteArray(Charsets.US_ASCII)
        internal val GIF_TRAILER = ";".toByteArray(Charsets.US_ASCII)
    }

    private fun header(buffer: ByteBuffer) = buffer.put(GIF_HEADER)

    private fun trailer(buffer: ByteBuffer) = buffer.put(GIF_TRAILER)

    var capacity = 1 shl 23

    /**
     * [ByteBuffer.capacity]
     */
    fun capacity(total: Int) = apply { capacity = total }

    var loop = 0

    /**
     * Netscape Looping Application Extension, 0 is infinite times
     * @see [ApplicationExtension.loop]
     */
    fun loop(count: Int) = apply { loop = count }

    var buffering = 0

    /**
     * Netscape Buffering Application Extension
     * @see [ApplicationExtension.buffering]
     */
    fun buffering(open: Boolean) = apply { buffering = if (open) 0x0001_0000 else 0x0000_0000 }

    var ratio = 0

    /**
     * Pixel Aspect Ratio
     * @see [LogicalScreenDescriptor.write]
     */
    fun ratio(size: Int) = apply {
        ratio = size
    }

    var global = ColorTable.Empty

    /**
     * GlobalColorTable
     * @see [OctTreeQuantizer.quantize]
     */
    fun table(bitmap: Bitmap) = apply {
        global = ColorTable(OctTreeQuantizer().quantize(bitmap, 256), true)
    }

    /**
     * GlobalColorTable
     */
    fun table(value: ColorTable) = apply {
        global = value
    }

    var options = AnimationFrameInfo(
        requiredFrame = -1,
        duration = 1000,
        // no use
        isFullyReceived = false,
        alphaType = ColorAlphaType.OPAQUE,
        isHasAlphaWithinBounds = false,
        disposalMethod = AnimationDisposalMode.UNUSED,
        blendMode = BlendMode.CLEAR,
        frameRect = IRect.makeXYWH(0, 0, 0, 0)
    )

    /**
     * GlobalFrameOptions
     */
    fun options(block: AnimationFrameInfo.() -> Unit): GIFBuilder = apply {
        options.apply(block)
    }

    var frames = ArrayList<Triple<Bitmap, ColorTable, AnimationFrameInfo>>()

    fun frame(
        bitmap: Bitmap,
        colors: ColorTable = ColorTable.Empty,
        block: AnimationFrameInfo.() -> Unit = {},
    ): GIFBuilder = apply {
        val rect = IRect.makeXYWH(0, 0, bitmap.width, bitmap.height)
        frames.add(Triple(bitmap, colors, options.withFrameRect(rect).apply(block)))
    }

    fun frame(
        bitmap: Bitmap,
        colors: ColorTable = ColorTable.Empty,
        info: AnimationFrameInfo,
    ) = apply {
        frames.add(Triple(bitmap, colors, info))
    }

    fun build(buffer: ByteBuffer) {
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        header(buffer)
        LogicalScreenDescriptor.write(buffer, width, height, global, ratio)
        if (loop >= 0) ApplicationExtension.loop(buffer, loop)
        if (buffering > 0) ApplicationExtension.buffering(buffer, buffering)
        for ((bitmap, colors, info) in frames) {
            val opaque = bitmap.any { it.alpha() == 0 }
            val table = when {
                colors.exists() -> colors
                global.exists() -> global
                else -> ColorTable(OctTreeQuantizer().quantize(bitmap, if (opaque) 255 else 256), true)
            }
            val transparency = if (opaque) table.transparency else null

            GraphicControlExtension.write(buffer, info.disposalMethod, false, transparency, info.duration)

            val result = AtkinsonDitherer.dither(bitmap, table.colors)

            @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
            ImageDescriptor.write(buffer, info.frameRect, table, table !== global, result)
        }
        trailer(buffer)
    }

    fun data(): Data {
        val data = Data.makeUninitialized(capacity)
        val buffer = BufferUtil.getByteBufferFromPointer(data.writableData(), capacity)
        build(buffer = buffer)

        return data.makeSubset(0, buffer.position())
    }

    fun build(): ByteArray {
        val buffer = ByteBuffer.allocate(capacity)
        build(buffer = buffer)

        return ByteArray(buffer.position()).also { buffer.get(it) }
    }
}