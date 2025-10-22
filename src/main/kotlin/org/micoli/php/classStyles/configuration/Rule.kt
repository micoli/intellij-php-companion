package org.micoli.php.classStyles.configuration

import io.swagger.v3.oas.annotations.media.Schema

class Rule {
    @Schema(description = ("Fully Qualified class name or interface"))
    var fqcns: Array<String> = arrayOf()
    @Schema(description = ("Style to apply if class implements one of the FQCNs"))
    var style: Style = Style()
}
