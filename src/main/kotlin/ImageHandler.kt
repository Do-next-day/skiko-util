@file:Suppress("UNUSED")

package top.e404.skiko

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.skia.Image

/**
 * 图片处理器
 */
interface ImageHandler {
    /**
     * 处理一张图片, 可能是gif的一帧, 也可能是单独的一张图片
     *
     * @param index 若是gif则index为帧数的下标, 若是图片则为0
     * @param count 帧数总数, 若为1则是
     * @param image 图片
     * @return 处理结果
     */
    suspend fun handleFrame(index: Int, count: Int, image: Image, data: ExtraData?, frame: Frame): Image

    /**
     * 解析图片的ByteArray
     *
     * @param bytes
     * @return
     */
    suspend fun handle(bytes: ByteArray, data: ExtraData?): HandlerResult {
        val frames = bytes.decodeToFrames()
        val result = kotlin.runCatching {
            if (frames.size == 1) {
                frames[0].apply {
                    handleAsImage(0, frames.size, data, ::handleFrame)
                }.bytes()
            } else frames.apply {
                runBlocking {
                    forEachIndexed { index, frame ->
                        launch {
                            frame.handleAsImage(index, frames.size, data, ::handleFrame)
                        }
                    }
                }
            }.encodeToBytes()
        }
        return HandlerResult(
            frames.size > 1,
            result.isSuccess,
            result.getOrNull(),
            result.exceptionOrNull()
        )
    }
}

/**
 * 代表图片处理的结果
 *
 * @property isGif 是否包含多帧图片
 * @property success 是否处理成功
 * @property result 若成功则为内容
 * @property throwable 若失败则为异常
 */
class HandlerResult(
    val isGif: Boolean,
    val success: Boolean,
    val result: ByteArray?,
    val throwable: Throwable?,
) {
    fun getOrNull() = result
    fun getOrThrow() = if (throwable != null) throw throwable else result!!
}

interface ExtraData