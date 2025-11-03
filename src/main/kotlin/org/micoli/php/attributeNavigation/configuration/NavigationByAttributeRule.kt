package org.micoli.php.attributeNavigation.configuration

import io.swagger.v3.oas.annotations.media.Schema

class NavigationByAttributeRule {
    @Schema(description = "FQCN of the attribute searched")
    var attributeFQCN: String = "\\Symfony\\Component\\Routing\\Attribute\\Route"
    @Schema(description = "Property of the attribute used") var propertyName: String = "path"
    @Schema(description = "Is this rule the default one?") var isDefault: Boolean = true
    @Schema(description = "File mask to search for attribute usages, separated by comma")
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
