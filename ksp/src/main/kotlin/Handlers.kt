package top.e404.skiko.ksp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import top.e404.skiko.annotation.ImageHandler

class FramesHandlerProcessor(environment: SymbolProcessorEnvironment) : SymbolProcessor {
    private val codeGenerator = environment.codeGenerator
    private val logger = environment.logger
    private val signName = ImageHandler::class.java.name

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val extSet = resolver.getSymbolsWithAnnotation(signName)
        val stream = try {
            codeGenerator.createNewFile(
                dependencies = Dependencies(false),
                packageName = "top.e404.skiko.handler",
                fileName = "handlers",
                extensionName = "kt"
            )
        } catch (e: Exception) {
            logger.warn("skip exists file top/e404/skiko/handler/handlers.kt")
            return emptyList()
        }
        logger.warn("process ${extSet.count()} ext")
        stream.bufferedWriter().use { bw ->
            bw.appendLine("package top.e404.skiko.handler").appendLine()
            bw.appendLine("// size: ${extSet.count()}")
            bw.appendLine("val handlerSet: Set<top.e404.skiko.frame.FramesHandler> = setOf(")
            extSet.forEach {
                it as KSClassDeclaration
                bw.append("    ").appendLine("${it.qualifiedName!!.asString()},")
            }
            bw.appendLine(")")
        }
        return emptyList()
    }
}

class FramesHandlerProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment) = FramesHandlerProcessor(environment)
}
