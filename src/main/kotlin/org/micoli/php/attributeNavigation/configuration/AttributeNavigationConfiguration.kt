package org.micoli.php.attributeNavigation.configuration

import io.swagger.v3.oas.annotations.media.Schema

class AttributeNavigationConfiguration {
    @Schema(description = "Array of navigation rules")
    var rules: Array<NavigationByAttributeRule> = arrayOf()
}
