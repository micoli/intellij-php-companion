package org.micoli.php.tasks.configuration.runnableTask;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "action", value = PostToggleAction.class),
    @JsonSubTypes.Type(name = "script", value = PostToggleScript.class),
})
public abstract class PostToggle extends RunnableTaskConfiguration {}
