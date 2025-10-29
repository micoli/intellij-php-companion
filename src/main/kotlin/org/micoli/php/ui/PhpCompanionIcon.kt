package org.micoli.php.ui

import com.intellij.openapi.util.IconLoader.getIcon
import javax.swing.Icon

interface PhpCompanionIcon {
    companion object {
        val Regexp: Icon = getIcon("expui/fileTypes/regexp.svg", PhpCompanionIcon::class.java)
        val Refresh: Icon = getIcon("expui/general/refresh.svg", PhpCompanionIcon::class.java)
        val Clean: Icon = getIcon("expui/actions/clearCash.svg", PhpCompanionIcon::class.java)
        val Back: Icon = getIcon("expui/actions/playBack.svg", PhpCompanionIcon::class.java)
        val Vendor: Icon = getIcon("expui/actions/vendor.svg", PhpCompanionIcon::class.java)
        val Copy: Icon = getIcon("expui/inline/copy.svg", PhpCompanionIcon::class.java)
        val AutoRefresh: Icon =
            getIcon("expui/actions/rerunAutomatically.svg", PhpCompanionIcon::class.java)
    }
}
