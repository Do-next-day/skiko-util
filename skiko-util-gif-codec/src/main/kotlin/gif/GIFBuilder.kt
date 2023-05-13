package top.e404.skiko.gif

import kotlinx.coroutines.*
import org.jetbrains.skia.*
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

    @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
    fun buildToBuffer(): ByteBuffer {
        val list = runBlocking {
            frames.map { (bitmap, colors, info) ->
                CoroutineScope(Dispatchers.Default).async {
                    val opaque = !bitmap.computeIsOpaque()
                    val table = when {
                        colors.exists() -> colors
                        global.exists() -> global
                        else -> ColorTable(OctTreeQuantizer().quantize(bitmap, if (opaque) 255 else 256), true)
                    }
                    val transparency = if (opaque) table.transparency else null
                    val result = AtkinsonDitherer.dither(bitmap, table.colors)

                    val descBuf = ImageDescriptor.toBuffer(info.frameRect, table, table !== global, result)
                    val buf = ByteBuffer.allocate(descBuf.limit() + 8)
                    buf.order(ByteOrder.LITTLE_ENDIAN)
                    GraphicControlExtension.write(
                        buf,
                        info.disposalMethod,
                        false,
                        transparency,
                        info.duration
                    ) // 8 Byte
                    for (i in 0 until descBuf.position()) buf.put(descBuf[i])
                    buf
                }
            }.awaitAll()
        }
        var size = GIF_HEADER.size +
                global.s() + 7 +
                list.sumOf { it.limit() } +
                GIF_TRAILER.size
        if (loop >= 0) size += 19
        if (buffering > 0) size += 21
        val buf = ByteBuffer.allocate(size)

        buf.order(ByteOrder.LITTLE_ENDIAN)

        header(buf) // GIF_HEADER.size
        LogicalScreenDescriptor.write(buf, width, height, global, ratio) // global.s()
        if (loop >= 0) ApplicationExtension.loop(buf, loop) // 19
        if (buffering > 0) ApplicationExtension.buffering(buf, buffering) // 21

        list.forEach {
            for (i in 0 until it.position()) buf.put(it[i])
        }

        trailer(buf)
        return buf
    }

    fun buildToData() = Data.makeFromBytes(buildToBuffer().array())
}