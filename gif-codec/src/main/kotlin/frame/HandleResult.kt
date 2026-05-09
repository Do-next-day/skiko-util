package top.e404.skiko.frame

/**
 * 代表图片处理的结果
 *
 * @property result 处理完的Frames, 若失败则为null
 * @property failMsg 失败的消息
 */
class HandleResult(
    val result: List<Frame>?,
    val failMsg: String?,
    val throwable: Throwable?,
) {
    companion object {
        fun fail(s: String) = HandleResult(null, s, null)
        suspend fun <T : List<Frame>> T.result(block: suspend T.() -> List<Frame>) = try {
            HandleResult(block(this), null, null)
        } catch (t: Throwable) {
            val m = if (t.message == null || t.message == "") "" else ": ${t.message}"
            HandleResult(null, "处理时出现异常(${t.javaClass.simpleName}$m)", t)
        }
    }

    val gif by lazy { result?.size != 1 }
    val success by lazy { result != null }
    fun getOrNull() = result
    fun getOrThrow(): List<Frame> {
        if (throwable != null) throw throwable
        if (result == null) throw Exception(failMsg)
        return result
    }
}
