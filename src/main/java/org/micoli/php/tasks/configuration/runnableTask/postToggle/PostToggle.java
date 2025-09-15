package org.micoli.php.tasks.configuration.runnableTask.postToggle;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.micoli.php.tasks.configuration.runnableTask.RunnableTaskConfiguration;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "builtin", value = PostToggleBuiltin.class),
    @JsonSubTypes.Type(name = "shell", value = PostToggleShell.class),
    @JsonSubTypes.Type(name = "script", value = PostToggleScript.class),
})
public abstract class PostToggle extends RunnableTaskConfiguration {}
