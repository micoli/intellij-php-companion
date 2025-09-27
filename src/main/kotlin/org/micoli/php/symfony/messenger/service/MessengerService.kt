package org.micoli.php.symfony.messenger.service

import com.intellij.codeInsight.completion.PlainPrefixMatcher
import com.intellij.openapi.components.Service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task.Backgroundable
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiSearchHelper
import com.intellij.psi.search.UsageSearchContext
import com.intellij.psi.util.PsiTreeUtil
import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.*
import java.util.*
import java.util.function.Consumer
import java.util.regex.Pattern
import java.util.stream.Collectors
import org.micoli.php.service.intellij.psi.PhpUtil.getPhpClassByFQN
import org.micoli.php.service.intellij.psi.PhpUtil.hasAttribute
import org.micoli.php.service.intellij.psi.PhpUtil.implementsInterfaces
import org.micoli.php.service.intellij.psi.PhpUtil.normalizeRootFQN
import org.micoli.php.symfony.messenger.configuration.SymfonyMessengerConfiguration

@Service(Service.Level.PROJECT)
class MessengerService(private val project: Project) {
    var configuration: SymfonyMessengerConfiguration? = SymfonyMessengerConfiguration()
    private var compiledMessageClassNamePatterns: Pattern? = null

    /**
     * this can be expensive if
     * (findHandlersByMessageName(phpClass.getProject(),phpClass.getFQN()).isEmpty()){ return true;
     * } return false:
     */
    fun isMessageClass(phpClass: PhpClass): Boolean {
        val className = phpClass.name

        if (this.implementsMessageInterfaces(phpClass)) {
            return true
        }

        return matchMessagePattern(className)
    }

    fun getHandledMessage(handlerClass: PhpClass): PhpClass? {
        val invokeMethod = getInvokableMethod(handlerClass) ?: return null

        val parameters = invokeMethod.parameters
        if (parameters.size != 1) {
            return null
        }

        val parameterType = parameters[0].type
        for (type in parameterType.types) {
            if (type.startsWith("\\")) {
                return getPhpClassByFQN(handlerClass.project, type)
            }
        }
        return null
    }

    fun getInvokableMethod(handlerClass: PhpClass): Method? {
        for (methodName in this.configuration!!.handlerMethods) {
            val invokeMethod = handlerClass.findOwnMethodByName(methodName)
            if (invokeMethod != null) {
                return invokeMethod
            }
        }
        return null
    }

    fun isHandlerClass(phpClass: PhpClass): Boolean {
        // Check naming pattern
        val className = phpClass.name
        if (matchMessagePattern(className)) {
            return true
        }

        // Check if implements MessageHandlerInterface
        if (this.implementsMessageHandlerInterfaces(phpClass)) {
            return true
        }

        // Check for #[AsMessageHandler] attribute
        if (this.hasHandlerAttribute(phpClass)) {
            return true
        }

        return hasInvokableMethodsWithMessageParameter(phpClass)
    }

    fun findDispatchCallsForMessageAsync(
        messageClassName: String,
        callback: Consumer<MutableCollection<MethodReference?>?>
    ) {
        ProgressManager.getInstance()
            .run(
                object : Backgroundable(project, "Finding usages", true) {
                    override fun run(indicator: ProgressIndicator) {
                        callback.accept(
                            getInstance(project).findDispatchCallsForMessage(messageClassName))
                    }
                })
    }

    fun findDispatchCallsForMessage(messageClassName: String): MutableCollection<MethodReference?> {
        val dispatchCalls: MutableList<MethodReference?> = ArrayList<MethodReference?>()

        val searchHelper = PsiSearchHelper.getInstance(project)
        val scope = GlobalSearchScope.projectScope(project)

        for (methodName in this.configuration!!.dispatchMethods) {
            searchHelper.processElementsWithWord(
                { element: PsiElement?, _: Int ->
                    val methodRef =
                        PsiTreeUtil.getParentOfType(element, MethodReference::class.java)
                    if (methodRef == null || methodName != methodRef.name) {
                        return@processElementsWithWord true
                    }

                    if (this.isMethodCalledWithMessageInstance(methodRef, messageClassName)) {
                        dispatchCalls.add(methodRef)
                    }
                    true
                },
                scope,
                methodName,
                UsageSearchContext.IN_CODE,
                true,
                false)
        }

        return dispatchCalls.stream().distinct().collect(Collectors.toList())
    }

    fun findHandlersByMessageName(messageClassName: String): MutableCollection<Method?> {
        val phpIndex = PhpIndex.getInstance(project)

        val handlers: MutableSet<Method?> = HashSet<Method?>()

        // Method 1: Find by interface implementation
        val interfaceHandlers: MutableCollection<PhpClass> = LinkedList<PhpClass>()
        for (interfaceFQN in this.configuration!!.messageHandlerInterfaces) {
            PhpIndex.getInstance(project).processAllSubclasses(interfaceFQN) { phpClass: PhpClass?
                ->
                interfaceHandlers.add(phpClass!!)
                true
            }
        }
        for (handler in interfaceHandlers) {
            if (handlesMessageType(handler, messageClassName)) {
                handlers.add(getInvokableMethod(handler))
            }
        }

        // Method 2: Scan all classes for __invoke method with right parameter
        // (This is expensive, so we might want to cache results)
        val allClasses =
            phpIndex.getAllClassFqns(PlainPrefixMatcher(this.configuration!!.projectRootNamespace))
        for (className in allClasses) {
            for (phpClass in phpIndex.getClassesByFQN(normalizeRootFQN(className))) {
                if (handlesMessageType(phpClass, messageClassName)) {
                    handlers.add(getInvokableMethod(phpClass))
                }
            }
        }

        return handlers
    }

    fun isMethodCalledWithMessageInstance(
        methodRef: MethodReference,
        messageClassName: String
    ): Boolean {
        var messageClassName = messageClassName
        messageClassName = normalizeRootFQN(messageClassName)

        val parameters = methodRef.parameters
        if (parameters.size == 0) {
            return false
        }
        val firstParam = parameters[0]

        if (firstParam is NewExpression) {
            val classRef = firstParam.classReference
            if (classRef != null) {
                return messageClassName == classRef.fqn
            }
        }

        if (firstParam is Variable) {
            return firstParam.type.toString().contains(messageClassName)
        }

        if (firstParam is MethodReference) {
            return firstParam.type.toString().contains(messageClassName)
        }

        if (firstParam is FieldReference) {
            return firstParam.type.toString().contains(messageClassName)
        }

        return false
    }

    private fun hasInvokableMethodsWithMessageParameter(phpClass: PhpClass): Boolean {
        val invokeMethod = getInvokableMethod(phpClass) ?: return false

        val parameters = invokeMethod.parameters
        return parameters.size == 1 && !parameters[0].type.toString().isEmpty()
    }

    private fun handlesMessageType(handler: PhpClass, messageClassName: String): Boolean {
        val invokeMethod = getInvokableMethod(handler) ?: return false

        val parameters = invokeMethod.parameters
        if (parameters.size != 1) {
            return false
        }

        val paramType = parameters[0].type
        return paramType.toString().contains(messageClassName)
    }

    fun extractMessageClassFromHandler(handlerMethod: Method): String? {
        val parameters = handlerMethod.parameters
        if (parameters.size == 0) {
            return null
        }

        val firstParam = parameters[0]
        val paramType = firstParam.type

        if (!paramType.isEmpty) {
            return paramType.toString()
        }

        return null
    }

    fun implementsMessageHandlerInterfaces(phpClass: PhpClass): Boolean {
        return implementsInterfaces(phpClass, this.configuration!!.messageHandlerInterfaces)
    }

    fun implementsMessageInterfaces(phpClass: PhpClass): Boolean {
        return implementsInterfaces(phpClass, this.configuration!!.messageInterfaces)
    }

    fun hasHandlerAttribute(phpClass: PhpClass): Boolean {
        return hasAttribute(phpClass, this.configuration!!.asMessageHandlerAttribute)
    }

    fun isDispatchMethod(methodName: String?): Boolean {
        return listOf(*this.configuration!!.dispatchMethods).contains(methodName)
    }

    fun isHandlerMethod(methodName: String?): Boolean {
        return listOf(*this.configuration!!.handlerMethods).contains(methodName)
    }

    fun matchMessagePattern(className: String): Boolean {
        if (this.compiledMessageClassNamePatterns == null) {
            return false
        }
        return this.compiledMessageClassNamePatterns!!.matcher(className).matches()
    }

    fun loadConfiguration(symfonyMessenger: SymfonyMessengerConfiguration?) {
        if (symfonyMessenger == null) {
            return
        }
        this.configuration = symfonyMessenger
        this.compiledMessageClassNamePatterns =
            Pattern.compile(configuration!!.messageClassNamePatterns)
    }

    fun getHandledMessages(handlerClass: PhpClass): MutableSet<String?> {
        val messages: MutableSet<String?> = HashSet<String?>()

        val invokeMethod = getInvokableMethod(handlerClass) ?: return messages

        val parameters = invokeMethod.parameters
        for (param in parameters) {
            val paramType = param.type.toString()
            if (!paramType.isEmpty() && paramType != "mixed") {
                messages.add(paramType)
            }
        }
        return messages
    }

    private fun hasInvokeMethodForMessage(phpClass: PhpClass, messageClassName: String): Boolean {
        if (!hasInvokableMethodsWithMessageParameter(phpClass)) {
            return false
        }

        return handlesMessageType(phpClass, messageClassName)
    }

    companion object {
        @JvmStatic
        fun getInstance(project: Project): MessengerService {
            return project.getService(MessengerService::class.java)
        }
    }
}
