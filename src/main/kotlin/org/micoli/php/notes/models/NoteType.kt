package org.micoli.php.notes.models

import com.intellij.icons.AllIcons
import javax.swing.Icon

enum class NoteType {
    PHP,
    JS,
    CSS,
    MD,
    HTML,
    YAML,
    JSON,
    XML,
    OTHER,
    PATH;

    fun getIcon(): Icon {
        return when (this) {
            PHP -> AllIcons.Language.Php
            JS -> AllIcons.FileTypes.JavaScript
            HTML -> AllIcons.FileTypes.Html
            CSS -> AllIcons.FileTypes.Css
            MD -> AllIcons.FileTypes.Manifest
            JSON -> AllIcons.FileTypes.Json
            XML -> AllIcons.FileTypes.Xml
            YAML -> AllIcons.FileTypes.Yaml
            OTHER -> AllIcons.FileTypes.Unknown
            PATH -> AllIcons.FileTypes.Any_type
        }
    }

    companion object {
        fun fileTypesEntries(): List<NoteType> {
            return NoteType.entries.filter { it != PATH && it != OTHER }.toList()
        }
    }
}
