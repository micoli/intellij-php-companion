package org.micoli.php.attributeNavigation.configuration

import io.swagger.v3.oas.annotations.media.Schema

class NavigationByAttributeRule {
    var attributeFQCN: String = "\\Symfony\\Component\\Routing\\Attribute\\Route"
    var propertyName: String = "path"
    var isDefault: Boolean = true
    var fileMask: String = "*.yaml,*.yml,*.php"

    @Schema(
        description = "How search is triggered", examples = ["search_everywhere", "find_in_file"])
    var actionType: String = "find_in_file"

    @Schema(
        description = "A groovy script to reformat raw attribute value",
        example =
            """
        return (value.replaceAll("(\\{.*?\\})", "[^/]*")+ ":");
        """)
    var formatterScript: String? = null
}
