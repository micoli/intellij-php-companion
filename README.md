[![Build Status](https://github.com/micoli/intellij-php-companion/actions/workflows/build.yml/badge.svg)](https://github.com/micoli/intellij-php-companion/actions)
[![JetBrains Plugins](https://img.shields.io/jetbrains/plugin/v/27762.svg)](https://plugins.jetbrains.com/plugin/27762-php-companion)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/27762.svg)](https://plugins.jetbrains.com/plugin/27762-php-companion)
[![Rating](https://img.shields.io/jetbrains/plugin/r/rating/27762.svg)](https://plugins.jetbrains.com/plugin/27762-php-companion)

<!-- Plugin description -->
# PHP Companion Intellij Plugin

A PhpStorm/IntelliJ plugin that enhances PHP development workflow with advanced navigation and code analysis features, particularly focused on Symfony Messenger pattern and peer navigation capabilities.

## Features

### 🚀 Symfony Messenger navigation
- **Smart Navigation**: Navigate from message dispatch calls directly to their corresponding handlers
- **Find Usages**: Discover all dispatch calls for a specific message class or handler method
- **Message Detection**: Automatically identify message classes based on naming patterns and interfaces
- **Handler Detection**: Recognize message handlers through interfaces, attributes, or naming conventions
- **Multi-dispatch Support**: Support for various dispatch method names (`dispatch`, `query`, `command`, `handle`)

### 🔗 Peer Navigation (code to test to code navigation)
- **Pattern-based Navigation**: Navigate between related classes using regex patterns
- **Flexible Mapping**: Define custom source-to-target class relationships
- **Go to Declaration**: Jump between peer classes with a single keystroke
- **Associate Navigation**: Navigate between associated classes based on custom relationships
- **Multi-direction Support**: Navigate bidirectionally between associated classes

### 🧑‍💻 Attribute Navigation
- **Attribute-based Navigation**: Open a intellij search with a formatted value of an attribute property
- **Flexible Mapping**: Define custom attribute property and a formatter to generate a custom intellij search pattern
- **Click and search**: Jump between peer classes with a single keystroke

### 📝 Source Export to LLM markdown
- **Markdown Export**: Export source code to markdown format optimized for LLMs
- **Code Context Preservation**: Maintain code structure and relationships in exported format, could also add all related classes found in specific namespaces.
- **Custom Formatting**: Configure export format settings
- **Selective Export**: Choose specific files or directories to export
- **.aiignore**: .aiignore file is used to filter files to export
- Freely inspired from https://github.com/keyboardsamurai/source-clipboard-export-intellij-plugin which was dedicated to Java project

### 🔍 CLI Dumper Parser
- **Command Output Analysis**: Parse and analyze CLI dumper output
- **Structured Data Extraction**: Convert raw CLI output into structured format (JSON ou short syntac PHP arrays)
- **Integration Support**: Seamless integration with development workflow

### Tool Window Panels
- Supports filtering and searching
- Double-click to navigate to command implementation
- Provides direct navigation to route definitions
- **Commands Panel**: Shows command descriptions and usage information
- **Routes Panel**: Displays route path, name, and methods
- **Doctrine Entities Panel**: Displays name, tableName and schema

### ⚙️ Configuration Management
- **Hot Reload**: Configuration changes are automatically detected and applied
- **Multiple Formats**: Support for JSON and YAML configuration files
- **Hierarchical Config**: Local configuration files can override global settings
- **Real-time Notifications**: Get notified when configuration is loaded or encounters errors

## Configuration

The plugin uses configuration files placed in your project root. The plugin will automatically detect and load configuration from any of these files (in order of precedence):

- `.php-companion.yaml`
- `.php-companion.json`
- `.php-companion.local.yaml`
- `.php-companion.local.json`

### Configuration Structure

#### Complete YAML Configuration Example

<!-- generateDocumentationExample("org.micoli.php.configuration.models.Configuration") -->
```yaml
attributeNavigation:
  rules:
    - actionType: find_in_file
      attributeFQCN: \Symfony\Component\Routing\Attribute\Route
      fileMask: '*.yaml,*.yml,*.php'
      formatterScript: |
        return (value.replaceAll("(\\{.*?\\})", "[^/]*")+ ":");
      isDefault: true
      propertyName: path
commandsConfiguration:
  attributeFQCN: \Symfony\Component\Console\Attribute\AsCommand
  enabled: true
  namespaces:
    - \App
    - \Application
consoleCleaner:
  patterns:
    - ''
doctrineEntitiesConfiguration:
  attributeFQCN: \Doctrine\ORM\Mapping\Table
  enabled: true
  namespaces:
    - \Domain
    - \Entity
exportSourceToMarkdown:
  contextualNamespaces:
    - App\Core\Models
  template: |
    [# th:each="file : ${files}"]
    ## [(${file.path})]

    ````[(${file.extension})]
    [(${file.content})]
    ````

    [/]
  useContextualNamespaces: true
  useIgnoreFile: true
openAPIConfiguration:
  enabled: true
  specificationRoots:
    - ''
peerNavigation:
  associates:
    - classA: \\App\\Tests\\Func\\(?<type>.*)\\Web\\(?<path>.*)\\ControllerTest
      classB: \\App\\(?<type>.*)\\Web\\(?<path>.*)\\Controller
  peers:
    - source: ''
      target: ''
routesConfiguration:
  attributeFQCN: \Symfony\Component\Routing\Attribute\Route
  enabled: true
  namespaces:
    - \App
    - \Application
symfonyMessenger:
  asMessageHandlerAttribute: Symfony\Component\Messenger\Attribute\AsMessageHandler
  dispatchMethods:
    - dispatch
    - query
    - command
    - handle
  handlerMethods:
    - __invoke
    - handle
  messageClassNamePatterns: .*(Message|Command|Query|Event|Input)$
  messageHandlerInterfaces:
    - Symfony\Component\Messenger\Handler\MessageHandlerInterface
  messageInterfaces:
    - ''
  projectRootNamespace: \App
  useNativeGoToDeclaration: false

```
<!-- generateDocumentationEnd -->


### Symfony Messenger Configuration

#### Properties
<!-- generateDocumentationProperties("org.micoli.php.symfony.messenger.configuration.SymfonyMessengerConfiguration","") -->
| Property                   | Description                                 |
| -------------------------- | ------------------------------------------- |
| asMessageHandlerAttribute  |                                             |
| dispatchMethods[]          | Method names used to dispatch messages      |
| handlerMethods[]           | Method names in handler classes             |
| messageClassNamePatterns   | Regex pattern to identify message classes   |
| messageHandlerInterfaces[] | Interfaces that handler classes implement   |
| messageInterfaces[]        | Interfaces that message classes implement   |
| projectRootNamespace       | Root namespace for scanning classes         |
| useNativeGoToDeclaration   | Disable ctrl+click to go to handler service |

- **asMessageHandlerAttribute**
  - **Default Value**: ``` Symfony\Component\Messenger\Attribute\AsMessageHandler ```
- **dispatchMethods[]**
  - Method names used to dispatch messages
- **handlerMethods[]**
  - Method names in handler classes
- **messageClassNamePatterns**
  - Regex pattern to identify message classes
  - **Default Value**: ``` .*(Message|Command|Query|Event|Input)$ ```
- **messageHandlerInterfaces[]**
  - Interfaces that handler classes implement
- **messageInterfaces[]**
  - Interfaces that message classes implement
- **projectRootNamespace**
  - Root namespace for scanning classes
  - **Default Value**: ``` \App ```
- **useNativeGoToDeclaration**
  - Disable ctrl+click to go to handler service
  - **Default Value**: ``` false ```
<!-- generateDocumentationEnd -->

#### Example
<!-- generateDocumentationExample("org.micoli.php.symfony.messenger.configuration.SymfonyMessengerConfiguration","symfonyMessenger") -->
```yaml
symfonyMessenger:
  asMessageHandlerAttribute: Symfony\Component\Messenger\Attribute\AsMessageHandler
  dispatchMethods:
  - dispatch
  - query
  - command
  - handle
  handlerMethods:
  - __invoke
  - handle
  messageClassNamePatterns: .*(Message|Command|Query|Event|Input)$
  messageHandlerInterfaces:
  - Symfony\Component\Messenger\Handler\MessageHandlerInterface
  messageInterfaces:
  - ''
  projectRootNamespace: \App
  useNativeGoToDeclaration: false
```
<!-- generateDocumentationEnd -->

### Peer Navigation Configuration

#### Properties
<!-- generateDocumentationProperties("org.micoli.php.peerNavigation.configuration.PeerNavigationConfiguration","") -->
| Property            | Description                                                                          |
| ------------------- | ------------------------------------------------------------------------------------ |
| associates[]        | Array of bidirectional navigation rules                                              |
| associates[].classA | Regex pattern with named groups matching first class FQN                             |
| associates[].classB | Pattern for second class FQN using `(?<groupName>.+)` substitution from named groups |
| peers[]             | Array of one-way navigation rules                                                    |
| peers[].source      | Regex pattern with named groups matching source class FQN                            |
| peers[].target      | Target class FQN pattern using `(?<groupName>.+)` substitution from named groups     |

- **associates[]**
  - Array of bidirectional navigation rules
- **associates[].classA**
  - Regex pattern with named groups matching first class FQN
  - **Example**: ``` \\App\\Tests\\Func\\(?<type>.*)\\Web\\(?<path>.*)\\ControllerTest ```
- **associates[].classB**
  - Pattern for second class FQN using `(?<groupName>.+)` substitution from named groups
  - **Example**: ``` \\App\\(?<type>.*)\\Web\\(?<path>.*)\\Controller ```
- **peers[]**
  - Array of one-way navigation rules
- **peers[].source**
  - Regex pattern with named groups matching source class FQN
- **peers[].target**
  - Target class FQN pattern using `(?<groupName>.+)` substitution from named groups
<!-- generateDocumentationEnd -->

#### Example
<!-- generateDocumentationExample("org.micoli.php.peerNavigation.configuration.PeerNavigationConfiguration","peerNavigation") -->
```yaml
peerNavigation:
  associates:
  - classA: \\App\\Tests\\Func\\(?<type>.*)\\Web\\(?<path>.*)\\ControllerTest
    classB: \\App\\(?<type>.*)\\Web\\(?<path>.*)\\Controller
  peers:
  - source: ''
    target: ''
```
<!-- generateDocumentationEnd -->

**Note**: Associates provide bidirectional navigation - you can navigate from classA to classB and vice versa.



#### Peer Navigation Examples

**Bidirectional Navigation between Entities and Repositories:**
```yaml
peerNavigation:
  associates:
    - classA: \\App\\Domain\\Entity\\(?<entity>.+)
      classB: \\App\\Domain\\Repository\\(?<entity>.+)Repository
```

**Bidirectional Navigation between Controllers and Services:**
```yaml
peerNavigation:
  associates:
    - classA: \\App\\Controller\\(?<controller>.+)Controller
      classB: \\App\\Service\\(?<controller>.+)Service
```

**One-way Navigation from Commands to CommandHandlers using Named Groups:**
```yaml
peerNavigation:
  peers:
    - source: \\App\\Application\\(?<domain>.+)\\Command\\(?<command>.+)Command
      target: \\App\\Application\\(?<domain>.+)\\CommandHandler\\(?<command>.+)CommandHandler
```

**One-way Navigation from Queries to QueryHandlers:**
```yaml
peerNavigation:
  peers:
    - source: \\App\\Application\\(?<domain>.+)\\Query\\(?<query>.+)Query
      target: \\App\\Application\\(?<domain>.+)\\QueryHandler\\(?<query>.+)QueryHandler
```

**Complex Example with Multiple Named Groups:**
```yaml
peerNavigation:
  peers:
    - source: \\App\\(?<layer>Application|Domain)\\(?<module>.+)\\Entity\\(?<entity>.+)
      target: \\App\\(?<layer>Application|Domain)\\(?<module>.+)\\Repository\\(?<entity>.+)Repository
  associates:
    - classA: \\App\\Domain\\(?<module>.+)\\Entity\\(?<entity>.+)
      classB: \\App\\Domain\\(?<module>.+)\\Factory\\(?<entity>.+)Factory
```

### Navigation by Attributes

#### Properties
<!-- generateDocumentationProperties("org.micoli.php.attributeNavigation.configuration.AttributeNavigationConfiguration","") -->
| Property                | Description                                     |
| ----------------------- | ----------------------------------------------- |
| rules[]                 |                                                 |
| rules[].actionType      | How search is triggered                         |
| rules[].attributeFQCN   |                                                 |
| rules[].fileMask        |                                                 |
| rules[].formatterScript | A groovy script to reformat raw attribute value |
| rules[].isDefault       |                                                 |
| rules[].propertyName    |                                                 |

- **rules[]**
- **rules[].actionType**
  - How search is triggered
  - **Default Value**: ``` find_in_file ```
- **rules[].attributeFQCN**
  - **Default Value**: ``` \Symfony\Component\Routing\Attribute\Route ```
- **rules[].fileMask**
  - **Default Value**: ``` *.yaml,*.yml,*.php ```
- **rules[].formatterScript**
  - A groovy script to reformat raw attribute value
  - **Example**: ```
      return (value.replaceAll("(\\{.*?\\})", "[^/]*")+ ":");
       ```
- **rules[].isDefault**
  - **Default Value**: ``` true ```
- **rules[].propertyName**
  - **Default Value**: ``` path ```
<!-- generateDocumentationEnd -->

#### Example
<!-- generateDocumentationExample("org.micoli.php.attributeNavigation.configuration.AttributeNavigationConfiguration","attributeNavigation") -->
```yaml
attributeNavigation:
  rules:
  - attributeFQCN: \Symfony\Component\Routing\Attribute\Route
    propertyName: path
    isDefault: true
    fileMask: '*.yaml,*.yml,*.php'
    actionType: find_in_file
    formatterScript: "\n        return (value.replaceAll(\"(\\\\{.*?\\\\})\", \"[^/]*\"\
      )+ \":\");\n        "
```
<!-- generateDocumentationEnd -->


### Export Source to Markdown Configuration

#### Properties
<!-- generateDocumentationProperties("org.micoli.php.exportSourceToMarkdown.configuration.ExportSourceToMarkdownConfiguration","") -->
| Property                | Description                                                                                                                                                                                                                              |
| ----------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| contextualNamespaces[]  | List of namespaces, if an import detected in an exported classes belong to one of those namespace, than the class is added in the context                                                                                                |
| template                | [Template Thymeleaf](https://www.thymeleaf.org/doc/tutorials/3.1/usingthymeleaf.html#standard-expression-syntax) used to generate markdown export. Accès aux variables : `files` (FileData properties `path`, `content`, et `extension`) |
| useContextualNamespaces |                                                                                                                                                                                                                                          |
| useIgnoreFile           |                                                                                                                                                                                                                                          |

- **contextualNamespaces[]**
  - List of namespaces, if an import detected in an exported classes belong to one of those namespace, than the class is added in the context
- **template**
  - [Template Thymeleaf](https://www.thymeleaf.org/doc/tutorials/3.1/usingthymeleaf.html#standard-expression-syntax) used to generate markdown export. Accès aux variables : `files` (FileData properties `path`, `content`, et `extension`)
  - **Default Value**: ``` [# th:each="file : ${files}"]
## [(${file.path})]

````[(${file.extension})]
[(${file.content})]
````

[/]
 ```
- **useContextualNamespaces**
  - **Default Value**: ``` true ```
- **useIgnoreFile**
  - **Default Value**: ``` true ```
<!-- generateDocumentationEnd -->

#### Example
<!-- generateDocumentationExample("org.micoli.php.exportSourceToMarkdown.configuration.ExportSourceToMarkdownConfiguration","exportSourceToMarkdown") -->
```yaml
exportSourceToMarkdown:
  contextualNamespaces:
  - App\Core\Models
  template: |
    [# th:each="file : ${files}"]
    ## [(${file.path})]

    ````[(${file.extension})]
    [(${file.content})]
    ````

    [/]
  useContextualNamespaces: true
  useIgnoreFile: true
```
<!-- generateDocumentationEnd -->

### Console cleaner

#### Properties
<!-- generateDocumentationProperties("org.micoli.php.consoleCleaner.configuration.ConsoleCleanerConfiguration","") -->
| Property   | Description                                                                                                                     |
| ---------- | ------------------------------------------------------------------------------------------------------------------------------- |
| patterns[] | Regular expression pattern for parsing output (if pattern start with ^and finished with $, then the whole line is stripped out) |

- **patterns[]**
  - Regular expression pattern for parsing output (if pattern start with ^and finished with $, then the whole line is stripped out)
<!-- generateDocumentationEnd -->

#### Example
<!-- generateDocumentationExample("org.micoli.php.consoleCleaner.configuration.ConsoleCleanerConfiguration","consoleCleaner") -->
```yaml
consoleCleaner:
  patterns:
  - ''
```
<!-- generateDocumentationEnd -->


### Tool windows Configuration

#### `routesConfiguration:`

#### Properties
<!-- generateDocumentationProperties("org.micoli.php.symfony.list.configuration.RoutesConfiguration","") -->
| Property      | Description                                  |
| ------------- | -------------------------------------------- |
| attributeFQCN | Attribute used to detect routes              |
| enabled       | Enabler for panel of routes                  |
| namespaces[]  | List of namespaces where routes are searched |

- **attributeFQCN**
  - Attribute used to detect routes
  - **Default Value**: ``` \Symfony\Component\Routing\Attribute\Route ```
- **enabled**
  - Enabler for panel of routes
  - **Default Value**: ``` true ```
- **namespaces[]**
  - List of namespaces where routes are searched
<!-- generateDocumentationEnd -->

#### Example
<!-- generateDocumentationExample("org.micoli.php.symfony.list.configuration.RoutesConfiguration","routesConfiguration") -->
```yaml
routesConfiguration:
  attributeFQCN: \Symfony\Component\Routing\Attribute\Route
  enabled: true
  namespaces:
  - \App
  - \Application
```
<!-- generateDocumentationEnd -->


#### `commandsConfiguration:`

#### Properties
<!-- generateDocumentationProperties("org.micoli.php.symfony.list.configuration.CommandsConfiguration","") -->
| Property      | Description                                            |
| ------------- | ------------------------------------------------------ |
| attributeFQCN | Attribute used to detect console commands              |
| enabled       | Enabler for panel of console commands                  |
| namespaces[]  | List of namespaces where console commands are searched |

- **attributeFQCN**
  - Attribute used to detect console commands
  - **Default Value**: ``` \Symfony\Component\Console\Attribute\AsCommand ```
- **enabled**
  - Enabler for panel of console commands
  - **Default Value**: ``` true ```
- **namespaces[]**
  - List of namespaces where console commands are searched
<!-- generateDocumentationEnd -->

#### Example

<!-- generateDocumentationExample("org.micoli.php.symfony.list.configuration.CommandsConfiguration","commandsConfiguration") -->
```yaml
commandsConfiguration:
  attributeFQCN: \Symfony\Component\Console\Attribute\AsCommand
  enabled: true
  namespaces:
  - \App
  - \Application
```
<!-- generateDocumentationEnd -->


#### `doctrineEntitiesConfiguration:`

#### Properties
<!-- generateDocumentationProperties("org.micoli.php.symfony.list.configuration.DoctrineEntitiesConfiguration","") -->
| Property      | Description                                             |
| ------------- | ------------------------------------------------------- |
| attributeFQCN | Attribute used to detect Entities                       |
| enabled       | Enabler for panel of doctrine entities                  |
| namespaces[]  | List of namespaces where doctrine entities are searched |

- **attributeFQCN**
  - Attribute used to detect Entities
  - **Default Value**: ``` \Doctrine\ORM\Mapping\Table ```
- **enabled**
  - Enabler for panel of doctrine entities
  - **Default Value**: ``` true ```
- **namespaces[]**
  - List of namespaces where doctrine entities are searched
<!-- generateDocumentationEnd -->

#### Example
<!-- generateDocumentationExample("org.micoli.php.symfony.list.configuration.DoctrineEntitiesConfiguration","doctrineEntitiesConfiguration") -->
```yaml
doctrineEntitiesConfiguration:
  attributeFQCN: \Doctrine\ORM\Mapping\Table
  enabled: true
  namespaces:
  - \Domain
  - \Entity
```
<!-- generateDocumentationEnd -->

#### `openAPIConfiguration`:

#### Properties
<!-- generateDocumentationProperties("org.micoli.php.symfony.list.configuration.OpenAPIConfiguration","openAPIConfiguration") -->
| Property             | Description                                           |
| -------------------- | ----------------------------------------------------- |
| enabled              | Enabler for panel of OAS routes                       |
| specificationRoots[] | List of root files of swagger/openapi yaml/json files |

- **enabled**
  - Enabler for panel of OAS routes
  - **Default Value**: ``` true ```
- **specificationRoots[]**
  - List of root files of swagger/openapi yaml/json files
<!-- generateDocumentationEnd -->

#### Example
<!-- generateDocumentationExample("org.micoli.php.symfony.list.configuration.OpenAPIConfiguration","openAPIConfiguration") -->
```yaml
openAPIConfiguration:
  enabled: true
  specificationRoots:
  - public/openapi.yaml
  - private/openapi.yaml
```
<!-- generateDocumentationEnd -->


#### `TasksConfiguration`:

#### Properties
<!-- generateDocumentationProperties("org.micoli.php.tasks.configuration.TasksConfiguration","tasksConfiguration") -->
| Property             | Description                                                                                                                                                                                   |
| -------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| enabled              | Enabler for panel of Task and actions                                                                                                                                                         |
| tasks[]              | Array of runnable task configurations available in the system. Each task must have a unique identifier to be referenced by tree or toolbar                                                    |
| tasks[].actionId     | Builtin actionId to execute                                                                                                                                                                   |
| tasks[].icon         | Path to the icon to display for this builtin task. Uses standard IntelliJ Platform icons                                                                                                      |
| tasks[].id           | Unique task identifier used for references in tree and toolbar. Must be unique among all tasks in the configuration                                                                           |
| tasks[].label        | Label displayed to user in the interface. User-friendly name describing the task function                                                                                                     |
| toolbar[]            | Array of tasks to display in the toolbar for quick access. Each element must reference an existing task via its taskId                                                                        |
| tree[]               | Hierarchical tree structure of tasks and folders for organization in the user interface. Can contain Task objects (referencing tasks by ID) and Path objects (folders containing other nodes) |
| tree[].label         | Label displayed for this folder in the hierarchical tree. User-friendly name for organizing tasks into logical groups                                                                         |
| tree[].tasks[]       | Array of child nodes contained in this folder. Can contain other folders (Path) or task references (Task)                                                                                     |
| watchers[]           | File watchers configuration that automatically trigger tasks when specified files are modified                                                                                                |
| watchers[].debounce  | Delay in milliseconds before task triggering after change detection. Prevents multiple executions during rapid successive modifications                                                       |
| watchers[].notify    | Indicates if a notification should be displayed to the user upon triggering. False by default to avoid too frequent notifications                                                             |
| watchers[].taskId    | Identifier of the task to execute when watched files are modified. Must match the ID of an existing task in the configuration                                                                 |
| watchers[].watches[] | Array of file patterns to watch. Supports wildcards and regular expressions to match file paths                                                                                               |

- **enabled**
  - Enabler for panel of Task and actions
  - **Example**: ``` true ```
  - **Default Value**: ``` false ```
- **tasks[]**
  - Array of runnable task configurations available in the system. Each task must have a unique identifier to be referenced by tree or toolbar
- **tasks[].actionId**
  - Builtin actionId to execute
  - **Example**: ``` $Copy ```
- **tasks[].icon**
  - Path to the icon to display for this builtin task. Uses standard IntelliJ Platform icons
  - **Default Value**: ``` debugger/threadRunning.svg ```
- **tasks[].id**
  - Unique task identifier used for references in tree and toolbar. Must be unique among all tasks in the configuration
  - **Example**: ``` aTaskId ```
- **tasks[].label**
  - Label displayed to user in the interface. User-friendly name describing the task function
  - **Example**: ``` First task ```
- **toolbar[]**
  - Array of tasks to display in the toolbar for quick access. Each element must reference an existing task via its taskId
- **tree[]**
  - Hierarchical tree structure of tasks and folders for organization in the user interface. Can contain Task objects (referencing tasks by ID) and Path objects (folders containing other nodes)
- **tree[].label**
  - Label displayed for this folder in the hierarchical tree. User-friendly name for organizing tasks into logical groups
- **tree[].tasks[]**
  - Array of child nodes contained in this folder. Can contain other folders (Path) or task references (Task)
- **watchers[]**
  - File watchers configuration that automatically trigger tasks when specified files are modified
- **watchers[].debounce**
  - Delay in milliseconds before task triggering after change detection. Prevents multiple executions during rapid successive modifications
  - **Default Value**: ``` 1000 ```
- **watchers[].notify**
  - Indicates if a notification should be displayed to the user upon triggering. False by default to avoid too frequent notifications
  - **Default Value**: ``` false ```
- **watchers[].taskId**
  - Identifier of the task to execute when watched files are modified. Must match the ID of an existing task in the configuration
- **watchers[].watches[]**
  - Array of file patterns to watch. Supports wildcards and regular expressions to match file paths
<!-- generateDocumentationEnd -->

#### Example
<!-- generateDocumentationExample("org.micoli.php.tasks.configuration.TasksConfiguration","tasksConfiguration") -->
```yaml
tasksConfiguration:
  enabled: false
  tasks:
  - type: builtin
    id: null
    label: null
    actionId: $Copy
    icon: debugger/threadRunning.svg
  - type: shell
    id: null
    label: null
    command: make clear-cache
    cwd: ''
    icon: debugger/threadRunning.svg
  - type: script
    id: null
    label: null
    source: ''
    extension: groovy
    icon: debugger/threadRunning.svg
  - type: observedFile
    icon: ''
    id: null
    label: null
    commentPrefix: '#'
    filePath: ''
    variableName: ''
    activeIcon: actions/inlayRenameInComments.svg
    inactiveIcon: actions/inlayRenameInCommentsActive.svg
    unknownIcon: expui/fileTypes/unknown.svg
    postToggle:
      type: builtin
      id: null
      label: null
      actionId: null
      icon: debugger/threadRunning.svg
  toolbar:
  - null
  tree:
  - type: path
    label: ''
    tasks:
    - null
    - type: task
      taskId: aTaskId
      label: aLabel
  - null
  watchers:
  - taskId: ''
    debounce: 1000
    notify: false
    watches:
    - ''
```
<!-- generateDocumentationEnd -->


#### `CodeStylesSynchronization`:

#### Properties
<!-- generateDocumentationProperties("org.micoli.php.codeStyle.configuration.CodeStylesSynchronizationConfiguration","codeStyleSynchronization") -->
| Property                | Description                                                                        |
| ----------------------- | ---------------------------------------------------------------------------------- |
| enabled                 | Enabler for panel of Code style synchronization                                    |
| styles[]                |                                                                                    |
| styles[].styleAttribute | Code style field property as in com.intellij.psi.codeStyle.CommonCodeStyleSettings |
| styles[].value          | a boolean value true/false or an int value                                         |

- **enabled**
  - Enabler for panel of Code style synchronization
  - **Example**: ``` true ```
  - **Default Value**: ``` false ```
- **styles[]**
- **styles[].styleAttribute**
  - Code style field property as in com.intellij.psi.codeStyle.CommonCodeStyleSettings
  - **Example**: ``` ALIGN_MULTILINE_PARAMETERS_IN_CALLS ```
- **styles[].value**
  - a boolean value true/false or an int value
  - **Example**: ``` false ```
<!-- generateDocumentationEnd -->

#### Example

<!-- generateDocumentationExample("org.micoli.php.codeStyle.configuration.CodeStylesSynchronizationConfiguration","codeStyleSynchronization") -->
```yaml
codeStyleSynchronization:
  enabled: false
  styles:
  - styleAttribute: ALIGN_MULTILINE_PARAMETERS_IN_CALLS
    value: 'false'
```
<!-- generateDocumentationEnd -->

### Scripting Classes

<!-- generateDocumentationSource("src/main/java/org/micoli/php/scripting","%s") -->
#### `UI`

known as `ui` in scripting engine

- `void alert(String message)`
  Displays a closable popup
   - `message`: the message to display.

- `void alert(String message, int delayInMs)`
  Displays a closable popup and automatically close it after a given delay
   - `message`: the message to display.
   - `delayInMs`: the delay in milliseconds before the popup is closed.

#### `FileSystem`

known as `fs` in scripting engine

- `void clearPath(String path)`
  Removes a path and it's sub content. Path must be ignored by GIT.
   - `path`: the relative filepath

- `void clearPath(String path, boolean mustBeGitIgnored)`
  Removes a path and it's sub content.
   - `path`: the relative filepath
   - `mustBeGitIgnored`: if false, the path will be removed even if it's not ignored by GIT.

#### `Core`

known as `core` in scripting engine

- `void runAction(String actionId)`
  Runs a registered action.
   - `actionId`: the ID of the action to run

- `void runActionInEditor(String actionId)`
  Activates the currently opened editor and runs a registered action.
   - `actionId`: the ID of the action to run


<!-- generateDocumentationEnd -->

### Configuration Tips

1. **Named Groups**: Use regex named groups `(?<name>...)` in patterns and reference them with `(?<name>...) (same expression)` in targets for better readability and maintainability
2. **Regex Escaping**: In YAML configuration, use double backslashes (`\\`) for namespace separators in regex patterns
3. **Local Overrides**: Use `.php-companion.local.*` files for project-specific settings that shouldn't be committed
4. **Hot Reload**: The plugin checks for configuration changes every 2 seconds
5. **Error Handling**: Configuration errors will be displayed as notifications in the IDE
6. **Peers vs Associates**:
    - Use `peers` for one-way navigation (source → target)
    - Use `associates` for bidirectional navigation (classA ↔ classB)
7. **Pattern Matching**: Both `peers` and `associates` support complex regex patterns with multiple named groups

<!-- Plugin description end -->

## Development

### Prerequisites

- Java 11+
- IntelliJ IDEA with Plugin DevKit
- Gradle


### Building the Plugin

```bash
# Build the plugin
./gradlew buildPlugin

# Run in development mode
./gradlew runIde

# Run tests
./gradlew test
```

### Key Components

#### Configuration System
- **ConfigurationFactory**: Loads and merges configuration files
- **GsonTools**: Handles JSON object merging with conflict resolution
- **Hot Reload**: Automatic configuration reloading every 2 seconds

#### Symfony Messenger
- **MessengerService**: Core service for message/handler detection and navigation
- **MessengerGotoDeclarationHandler**: Handles navigation from dispatch calls to handlers
- **MessengerFindUsagesHandler**: Finds all dispatch calls for messages/handlers
- **PHPHelper**: Utility methods for PHP class analysis

#### Peer Navigation
- **PeerNavigationGotoDeclarationHandler**: Handles navigation between peer classes
- **Regex-based Matching**: Flexible pattern matching for class relationships

### Contributing

1. **Fork the Repository**: Create your own fork of the project
2. **Create Feature Branch**: `git checkout -b feature/your-feature-name`
3. **Follow Code Style**: Use existing code formatting and conventions
4. **Add Tests**: Include unit tests for new functionality
5. **Update Documentation**: Update README and inline documentation
6. **Submit Pull Request**: Create a PR with clear description of changes

### Code Style Guidelines

- Use standard Java naming conventions
- Add JavaDoc comments for public methods
- Keep methods focused and single-purpose
- Use meaningful variable and method names
- Handle exceptions appropriately with user-friendly error messages

### Testing

The plugin includes unit tests for core functionality. When adding new features:

1. Add corresponding unit tests
2. Test with different PHP project structures
3. Verify configuration loading and hot reload
4. Test error handling scenarios

### Debugging

To debug the plugin:

1. Use `./gradlew runIde` to launch a development instance
2. Set breakpoints in your IDE
3. Use IntelliJ's internal logging: `Help → Show Log in Files`
4. Enable debug notifications in `Notification.java`

### Architecture Notes

- **Extension Points**: Uses IntelliJ's extension point system for handlers
- **Project Components**: Manages lifecycle through ProjectComponent interface
- **PSI Integration**: Leverages PhpStorm's PSI (Program Structure Interface) for code analysis
- **Background Processing**: Configuration loading runs on background threads
- **Event-Driven**: Uses IntelliJ's event system for real-time updates

### Future Enhancements

- Support for PHP 8 attributes parsing
- Enhanced caching for better performance
- Additional Symfony component integrations
- Visual configuration editor
- Code generation templates
