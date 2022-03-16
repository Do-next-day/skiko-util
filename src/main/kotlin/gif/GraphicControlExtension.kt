// 参考自 Mirai Skija Plugin by cssxsh https://github.com/cssxsh/mirai-skija-plugin
@file:Suppress("UNUSED")

package top.e404.skiko.gif

import java.nio.*

object GraphicControlExtension {
    private const val INTRODUCER = 0x21
    private const val LABEL = 0xF9
    private const val BLOCK_SIZE = 0x04
    private const val TERMINATOR = 0x00

    private fun block(
        buffer: ByteBuffer,
        flags: Byte,
        duration: Short,
        transparencyIndex: Byte,
    ) {
        buffer.put(INTRODUCER.asUnsignedByte())
        buffer.put(LABEL.asUnsignedByte())
        buffer.put(BLOCK_SIZE.asUnsignedByte())
        buffer.put(flags)
        buffer.putShort(duration)
        buffer.put(transparencyIndex)
        buffer.put(TERMINATOR.asUnsignedByte())
    }

    fun write(
        buffer: ByteBuffer,
        disposalMethod: DisposalMethod,
        userInput: Boolean,
        transparencyIndex: Int?,
        duration: Int
    ) {
        // Not Interlaced Images
        var flags = 0x0000

        flags = flags or disposalMethod.flag
        if (userInput) flags = flags or 0x0002
        if (transparencyIndex in 0 until 256) flags = flags or 0x0001

        block(
            buffer = buffer,
            flags = flags.asUnsignedByte(),
            duration = (duration / 100).asUnsignedShort(),
            transparencyIndex = (transparencyIndex ?: 0).asUnsignedByte()
        )
    }
}