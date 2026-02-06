package org.micoli.php.symfony.messenger.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocComment
import com.jetbrains.php.lang.documentation.phpdoc.psi.PhpDocType
import com.jetbrains.php.lang.psi.PhpFile
import com.jetbrains.php.lang.psi.elements.Method
import com.jetbrains.php.lang.psi.elements.MethodReference
import com.jetbrains.php.lang.psi.elements.PhpUseList
import com.jetbrains.php.lang.psi.elements.Try
import org.micoli.php.service.intellij.psi.PhpUtil
import org.micoli.php.symfony.messenger.service.MessengerService

@Service(Service.Level.PROJECT)
class CommandBusExceptionService(private val project: Project) {
    var enabled: Boolean = false

    fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (!enabled) return

        if (element !is MethodReference) return

        val messengerService = MessengerService.getInstance(project)
        if (!messengerService.enabled) return
        if (!messengerService.isDispatchMethod(element.name)) return

        val messageClassName = PhpUtil.getFirstParameterType(element.parameters) ?: return

        val handlers = messengerService.findHandlersByMessageName(messageClassName)
        if (handlers.isEmpty()) return

        val handlerThrows = mutableSetOf<String>()
        for (handler in handlers) {
            if (handler == null) continue
            handlerThrows.addAll(extractThrowsFromDocComment(handler))
        }
        if (handlerThrows.isEmpty()) return

        val containingMethod = PsiTreeUtil.getParentOfType(element, Method::class.java) ?: return

        val existingThrows = extractThrowsFromDocComment(containingMethod)
        val caughtExceptions = extractCaughtExceptions(element)

        val missingThrows =
            handlerThrows
                .filter { thrown ->
                    val normalizedThrown = normalizeForComparison(thrown)
                    existingThrows.none { normalizeForComparison(it) == normalizedThrown } &&
                        caughtExceptions.none { normalizeForComparison(it) == normalizedThrown }
                }
                .toSet()

        if (missingThrows.isEmpty()) return

        val message =
            "Missing @throws for dispatched handler exceptions: ${
            missingThrows.joinToString(", ") { PhpUtil.getShortClassName(it) }
        }"

        holder
            .newAnnotation(HighlightSeverity.WARNING, message)
            .range(element.textRange)
            .withFix(AddMissingThrowsQuickFix(containingMethod, missingThrows))
            .create()
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): CommandBusExceptionService {
            return project.getService(CommandBusExceptionService::class.java)
        }

        fun extractThrowsFromDocComment(method: Method): Set<String> {
            val docComment = method.docComment as? PhpDocComment ?: return emptySet()
            val throwsTags = docComment.getTagElementsByName("@throws")
            if (throwsTags.isEmpty()) return emptySet()

            val result = mutableSetOf<String>()
            for (tag in throwsTags) {
                val docTypes = PsiTreeUtil.findChildrenOfType(tag, PhpDocType::class.java)
                for (docType in docTypes) {
                    val resolved = resolveDocType(docType, method)
                    if (resolved.isNotBlank()) {
                        result.add(resolved)
                    }
                }
            }
            return result
        }

        private fun resolveDocType(docType: PhpDocType, method: Method): String {
            // If it's already absolute (starts with \), use PSI's FQN directly
            if (docType.isAbsolute) {
                return PhpUtil.normalizeRootFQN(docType.fqn)
            }

            // For relative names, try to resolve via use statements
            val rawName = docType.text
            val phpFile = method.containingFile as? PhpFile

            if (phpFile != null) {
                val useMap = buildUseMap(phpFile)

                if (!rawName.contains("\\")) {
                    val resolved = useMap[rawName]
                    if (resolved != null) {
                        return PhpUtil.normalizeRootFQN(resolved)
                    }
                }

                // Fall back to namespace-relative resolution
                val namespace = phpFile.mainNamespaceName
                if (namespace.isNotEmpty()) {
                    return PhpUtil.normalizeRootFQN("$namespace\\$rawName")
                }
            }

            return PhpUtil.normalizeRootFQN(rawName)
        }

        private fun buildUseMap(phpFile: PhpFile): Map<String, String> {
            val useMap = mutableMapOf<String, String>()
            PsiTreeUtil.processElements(phpFile) { element ->
                if (element is PhpUseList) {
                    for (use in element.declarations) {
                        val shortName = use.aliasName ?: use.name ?: return@processElements true
                        useMap[shortName] = use.fqn
                    }
                }
                true
            }
            return useMap
        }

        fun extractCaughtExceptions(element: PsiElement): Set<String> {
            val tryStatement =
                PsiTreeUtil.getParentOfType(element, Try::class.java) ?: return emptySet()

            val result = mutableSetOf<String>()
            for (catchClause in tryStatement.catchClauses) {
                for (exceptionType in catchClause.exceptionTypes) {
                    val fqn = exceptionType.fqn
                    if (!fqn.isNullOrBlank()) {
                        result.add(PhpUtil.normalizeRootFQN(fqn))
                    }
                }
            }
            return result
        }

        private fun normalizeForComparison(fqcn: String): String {
            return PhpUtil.normalizeRootFQN(fqcn).lowercase()
        }
    }
}
