package org.micoli.php.attributeNavigation.configuration;

import io.swagger.v3.oas.annotations.media.Schema;

public final class NavigationByAttributeRule {
    public String attributeFQCN = "\\Symfony\\Component\\Routing\\Attribute\\Route";
    public String propertyName = "path";
    public Boolean isDefault = true;
    public String fileMask = "*.yaml,*.yml,*.php";

    @Schema(
            description = "How search is triggered",
            examples = {"search_everywhere", "find_in_file"})
    public String actionType = "find_in_file";

    @Schema(
            description = "A groovy script to reformat raw attribute value",
            example = """
        return (value.replaceAll("(\\\\{.*?\\\\})", "[^/]*")+ ":");
        """)
    public String formatterScript = null;
}
