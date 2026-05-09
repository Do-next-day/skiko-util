package top.e404.skiko.frame

/**
 * 图片处理器
 */
interface FramesHandler {
    /**
     * 处理器名字
     */
    val name: String

    /**
     * 处理器名字匹配正则
     */
    val regex: Regex

    /**
     * 处理Frames
     *
     * @param frames Frames
     * @param args 参数
     * @return 处理结果
     */
    suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ): HandleResult

    @Suppress("UNUSED")
    suspend fun handleBytes(
        bytes: ByteArray,
        args: MutableMap<String, String>
    ) = try {
        handleFrames(
            bytes.decodeToFrames(),
            args
        )
    } catch (t: Throwable) {
        HandleResult(null, t.message, t)
    }
}