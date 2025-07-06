package org.micoli.php.attributeNavigation.configuration;

public final class NavigationByAttributeRule {
    public String attributeFQCN = "\\Symfony\\Component\\Routing\\Attribute\\Route";
    public String propertyName = "path";
    public String actionType = "find_in_file";
    public Boolean isDefault = true;
    public String fileMask = "*.yaml,*.yml,*.php";
    public String formatterScript = null;
}
