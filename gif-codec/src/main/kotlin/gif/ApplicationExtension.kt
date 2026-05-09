package top.e404.skiko.gif

import java.nio.ByteBuffer

object ApplicationExtension {
    private val INTRODUCER = 0x21.asUnsignedByte()

    /**
     * 标签
     */
    private val LABEL = 0xFF.asUnsignedByte()

    /**
     * 块大小
     */
    private val BLOCK_SIZE = 0x0B.asUnsignedByte()

    /**
     * 结束标识符
     */
    private val TERMINATOR = 0x00.asUnsignedByte()

    /**
     * 将数据按数据快格式写入缓冲区
     *
     * @param buffer 缓冲区
     * @param identifier 标识符
     * @param authentication 验证
     * @param data 数据
     */
    private fun write(
        buffer: ByteBuffer,
        identifier: String,
        authentication: String,
        data: ByteArray
    ) = buffer.apply {
        put(INTRODUCER)
        put(LABEL)
        put(BLOCK_SIZE)
        put(identifier.toByteArray(Charsets.US_ASCII)) // 8 byte
        put(authentication.toByteArray(Charsets.US_ASCII)) // 3 byte
        put(data.size.asUnsignedByte())
        put(data)
        put(TERMINATOR)
    }

    fun loop(
        buffer: ByteBuffer,
        count: Int
    ) = write(
        buffer = buffer,
        identifier = "NETSCAPE",
        authentication = "2.0",
        data = byteArrayOf(
            0x01,
            count.ushr(8).toByte(),
            count.ushr(0).toByte()
        )
    )

    fun buffering(
        buffer: ByteBuffer,
        capacity: Int
    ) = write(
        buffer = buffer,
        identifier = "NETSCAPE",
        authentication = "2.0",
        data = byteArrayOf(
            0x01,
            capacity.ushr(24).toByte(),
            capacity.ushr(16).toByte(),
            capacity.ushr(8).toByte(),
            capacity.ushr(0).toByte()
        )
    )

    fun profile(buffer: ByteBuffer, data: ByteArray) {
        write(
            buffer = buffer,
            identifier = "ICCRGBG1",
            authentication = "012",
            data = data
        )
    }
}