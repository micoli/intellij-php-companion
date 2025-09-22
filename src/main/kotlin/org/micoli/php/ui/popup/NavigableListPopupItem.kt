package org.micoli.php.ui.popup

import com.intellij.pom.Navigatable

interface NavigableListPopupItem : Navigatable {
    fun getText(): String
}
