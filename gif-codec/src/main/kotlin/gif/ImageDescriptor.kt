package top.e404.skiko.gif

import org.jetbrains.skia.IRect
import java.nio.ByteBuffer
import java.nio.ByteOrder

object ImageDescriptor {
    private const val SEPARATOR = 0x002C
    private const val TERMINATOR = 0x0000

    private fun block(
        buffer: ByteBuffer,
        left: Short,
        top: Short,
        width: Short,
        height: Short,
        flags: Byte,
    ) {
        buffer.put(SEPARATOR.asUnsignedByte())
        buffer.putShort(left)
        buffer.putShort(top)
        buffer.putShort(width)
        buffer.putShort(height)
        buffer.put(flags)
    }

    private fun data(
        buffer: ByteBuffer,
        min: Int,
        data: ByteArray,
    ) { // 1 + (data.size / 255) * (1 + data.size)
        buffer.put(min.asUnsignedByte()) // 1 Byte
        for (index in data.indices step 255) {
            val size = minOf(data.size - index, 255)
            buffer.put(size.asUnsignedByte()) // 1 Byte
            buffer.put(data, index, size)
        }
    }

    internal fun write(
        buffer: ByteBuffer,
        rect: IRect,
        table: ColorTable,
        local: Boolean,
        image: IntArray
    ) {
        // Not Interlaced Images
        var flags = 0x00

        if (local) {
            flags = 0x80 or table.size()
            if (table.sort) {
                flags = flags or 0x10
            }
        }

        block(
            buffer = buffer,
            left = rect.left.asUnsignedShort(),
            top = rect.top.asUnsignedShort(),
            width = rect.width.asUnsignedShort(),
            height = rect.height.asUnsignedShort(),
            flags = flags.asUnsignedByte()
        )

        if (local) table.write(buffer)

        val (min, lzw) = LZWEncoder(table, image).encode()

        data(buffer, min, lzw)

        buffer.put(TERMINATOR.asUnsignedByte())
    }

    private fun size(data: ByteArray): Int {
        var s = 1
        for (index in data.indices step 255) {
            val size = minOf(data.size - index, 255)
            s += 1
            s += size
        }
        return s
    }

    fun toBuffer(
        rect: IRect,
        table: ColorTable,
        local: Boolean,
        image: IntArray
    ): ByteBuffer {
        val (min, lzw) = LZWEncoder(table, image).encode()

        // Not Interlaced Images
        var flags = 0x00

        if (local) {
            flags = 0x80 or table.size()
            if (table.sort) {
                flags = flags or 0x10
            }
        }

        val s = size(lzw)

        val buffer = ByteBuffer.allocate(11 + table.s() + s)
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        block(
            buffer = buffer,
            left = rect.left.asUnsignedShort(),
            top = rect.top.asUnsignedShort(),
            width = rect.width.asUnsignedShort(),
            height = rect.height.asUnsignedShort(),
            flags = flags.asUnsignedByte()
        ) // 10

        if (local) table.write(buffer) // (colors.capacity() - colors.size) * 3 + 3

        data(buffer, min, lzw)

        buffer.put(TERMINATOR.asUnsignedByte())

        return buffer
    }
}