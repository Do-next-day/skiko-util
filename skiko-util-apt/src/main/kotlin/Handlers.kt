package top.e404.skiko.apt.annotation

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
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
        Handlers.apply {
            roundEnv.filterHasAnnotation(ImageHandler::class.java)
                .forEach { handlers.add(it) }
            save()
        }
        return true
    }
}

@Serializable
object Handlers {
    private val f = File("src/main/resources/handlers.txt")
    val handlers = if (f.exists()) f.readText().split("\n").toMutableList()
    else mutableListOf()

    fun save() = f.writeText(handlers.joinToString("\n"))
}