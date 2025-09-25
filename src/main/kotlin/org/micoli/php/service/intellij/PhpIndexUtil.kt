package org.micoli.php.service.intellij

import com.jetbrains.php.PhpIndex
import com.jetbrains.php.lang.psi.elements.PhpClass

object PhpIndexUtil {
    fun getAllSubclasses(phpIndex: PhpIndex, clazz: String): MutableCollection<PhpClass> {
        val phpClasses: MutableCollection<PhpClass> = ArrayList()

        phpIndex.processAllSubclasses(clazz) {
            phpClasses.add(it)
            true
        }

        return phpClasses
    }
}
