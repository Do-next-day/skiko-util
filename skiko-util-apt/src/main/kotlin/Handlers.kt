package top.e404.skiko.apt.annotation

import java.io.File
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

/**
 * 标记为Frames处理器
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ImageHandler

@SupportedAnnotationTypes("top.e404.skiko.apt.annotation.ImageHandler")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
class FramesHandlerProcessor : AbstractProcessor() {
    override fun process(
        annotations: Set<TypeElement>,
        roundEnv: RoundEnvironment,
    ): Boolean {
        val list = roundEnv.filterHasAnnotation(ImageHandler::class.java)
        if (list.isEmpty()) return true
        File("src/main/resources/handlers.txt").writeText(list.joinToString("\n"))
        return true
    }
}