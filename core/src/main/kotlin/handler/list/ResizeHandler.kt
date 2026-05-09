package top.e404.skiko.handler.list

import top.e404.skiko.annotation.ImageHandler
import top.e404.skiko.frame.Frame
import top.e404.skiko.frame.FramesHandler
import top.e404.skiko.frame.HandleResult.Companion.result
import top.e404.skiko.frame.common

/**
 * 缩放图片, 若数字为负数则作为百分比处理
 */
@ImageHandler
object ResizeHandler : FramesHandler {
    override val name = "缩放"
    override val regex = Regex("(?i)缩放|resize|sf")
    override suspend fun handleFrames(
        frames: MutableList<Frame>,
        args: MutableMap<String, String>,
    ) = frames.result { common(args) }
}