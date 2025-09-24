package org.micoli.php.ui

import com.intellij.openapi.util.IconLoader.getIcon
import javax.swing.Icon

interface PhpCompanionIcon {
    companion object {
        val Regexp: Icon = getIcon("expui/fileTypes/regexp.svg", PhpCompanionIcon::class.java)
        val Refresh: Icon =
            getIcon("expui/actions/buildAutoReloadChanges.svg", PhpCompanionIcon::class.java)
    }
}
