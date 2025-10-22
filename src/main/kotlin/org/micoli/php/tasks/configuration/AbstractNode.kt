package org.micoli.php.tasks.configuration

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = Task::class)
@JsonSubTypes(
    JsonSubTypes.Type(name = "path", value = Path::class),
    JsonSubTypes.Type(name = "task", value = Task::class),
)
abstract class AbstractNode
