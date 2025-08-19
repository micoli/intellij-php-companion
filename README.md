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

```yaml
attributeNavigation:
  rules:
    - attributeFQCN: \Symfony\Component\Routing\Attribute\Route
      propertyName: path
      fileMask: "*.yaml,*.yml,*.php"
      formatterScript: |
        return (value
          .replaceAll("(\\{.*?\\})", "[^/]*")
          + ":"
        );

symfonyMessenger:
  useNativeGoToDeclaration: false
  projectRootNamespace: \App
  messageClassNamePatterns: .*(Message|Command|Query|Event|Input)$
  messageInterfaces:
    - App\Shared\Application\Message\MessageInterface
  messageHandlerInterfaces:
    - Symfony\Component\Messenger\Handler\MessageHandlerInterface
  dispatchMethods:
    - dispatch
    - query
    - command 
    - handle
  handlerMethods:
    - __invoke
    - handle

peerNavigation:
  associates:
    - classA: \\App\\Domain\\Entity\\(?<entity>.+)
      classB: \\App\\Domain\\Repository\\(?<entity>.+)Repository
    - classA: \\App\\Domain\\Entity\\(?<entity>.+)
      classB: \\App\\Domain\\Factory\\(?<entity>.+)Factory
    - classA: \\App\\Application\\(?<domain>.+)\\Command\\(?<command>.+)Command
      classB: \\App\\Application\\(?<domain>.+)\\CommandHandler\\(?<command>.+)CommandHandler
  peers:
    - source: \\App\\Application\\(?<domain>.+)\\Command\\(?<command>.+)Command
      target: \\App\\Application\\(?<domain>.+)\\CommandHandler\\(?<command>.+)CommandHandler
    - source: \\App\\Application\\(?<domain>.+)\\Query\\(?<query>.+)Query
      target: \\App\\Application\\(?<domain>.+)\\QueryHandler\\(?<query>.+)QueryHandler

routesConfiguration:
  attributeFQCN: Symfony\Component\Routing\Annotation\Route
  namespaces:
    - App\UserInterface\Web

commandsConfiguration:
  attributeFQCN: Symfony\Component\Console\Attribute\AsCommand
  namespaces:
    - App\UserInterface\Cli

doctrineEntitiesConfiguration:
  namespaces:
    - App\Core\Models
```

### Symfony Messenger Configuration

| Property | Type | Default | Description                                 |
|----------|------|---------|---------------------------------------------|
| `projectRootNamespace` | string | `"\\App"` | Root namespace for scanning classes         |
| `messageClassNamePatterns` | string | `".*(Message\|Command\|Query\|Event\|Input)$"` | Regex pattern to identify message classes   |
| `messageInterfaces` | string[] | `[]` | Interfaces that message classes implement   |
| `messageHandlerInterfaces` | string[] | `["Symfony\\Component\\Messenger\\Handler\\MessageHandlerInterface"]` | Interfaces that handler classes implement   |
| `dispatchMethods` | string[] | `["dispatch", "query", "command", "handle"]`                          | Method names used to dispatch messages      |
| `handlerMethods` | string[] | `["__invoke", "handle"]`                                              | Method names in handler classes             |
| `useNativeGoToDeclaration` | boolean  | `false`                                                               | Disable ctrl+click to go to handler service |

### Peer Navigation Configuration

| Property | Type | Description                                                                          |
|----------|------|--------------------------------------------------------------------------------------|
| `associates` | object[] | Array of bidirectional navigation rules                                              |
| `associates[].classA` | string | Regex pattern with named groups matching first class FQN                             |
| `associates[].classB` | string | Pattern for second class FQN using `(?<groupName>.+)` substitution from named groups |
| `peers` | object[] | Array of one-way navigation rules                                                    |
| `peers[].source` | string | Regex pattern with named groups matching source class FQN                            |
| `peers[].target` | string | Target class FQN pattern using `(?<groupName>.+)` substitution from named groups     |

### Associate Navigation Configuration

| Property | Type | Description                                                                          |
|----------|------|--------------------------------------------------------------------------------------|
| `classA` | string | Regex pattern with named groups matching first class FQN                             |
| `classB` | string | Pattern for second class FQN using `(?<groupName>.+)` substitution from named groups |

**Note**: Associates provide bidirectional navigation - you can navigate from classA to classB and vice versa.

### CLI Dumper Configuration

| Property | Type | Description |
|----------|------|-------------|
| `parsers` | array | Array of parser objects |
| `parsers[].name` | string | Name of the parser |
| `parsers[].pattern` | string | Regular expression pattern for parsing output |
| `parsers[].groups` | array | Groups extracted from the parsed output |

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

### Export Source to Markdown Configuration

| Property               | Type     | Description                                                                                                                                                                                                                              |
|------------------------|----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `template`             | string   | [Template Thymeleaf](https://www.thymeleaf.org/doc/tutorials/3.1/usingthymeleaf.html#standard-expression-syntax) used to generate markdown export. Accès aux variables : `files` (FileData properties `path`, `content`, et `extension`) |
| `contextualNamespaces` | string[] | List of namespaces, if an import detected in an exported classes belong to one of those namespace, than the class is added in the context                                                                                                |

#### Configuration Example

```yaml
exportSourceToMarkdown:
  contextualNamespaces:
     -  App\Core\Models
  template: |
     [(${#strings.isEmpty(files) ? '' : ''})]
     [# th:each="file : ${files}"]
     ## [(${file.path})]

     ```[(${file.extension})]
     [(${file.content})]
     ```

     [/]
```

### Tool windows Configuration

#### `routesConfiguration:`

| Property         | Type     | Description                                  |
|------------------|----------|----------------------------------------------|
| `attributeFQCN`  | string   | Attribute used to detect routes (default: `Symfony\Component\Routing\Attribute\Route`)             |
| `namespaces` | string[] | List of namespaces where routes are searched |


#### `commandsConfiguration:`

| Property         | Type     | Description                                    |
|------------------|----------|------------------------------------------------|
| `attributeFQCN`  | string   | Attribute used to detect commands (default: `Symfony\Component\Console\Attribute\AsCommand`)             |
| `namespaces` | string[] | List of namespaces where commands are searched |


#### `doctrineEntitiesConfiguration:`

| Property         | Type     | Description                                            |
|------------------|----------|--------------------------------------------------------|
| `attributeFQCN`  | string   | Attribute used to detect doctrine entities (default: `Doctrine\ORM\Mapping\Table`) |
| `namespaces` | string[] | List of namespaces where entities are searched         |

#### Configuration Example

```yaml
routesConfiguration:
  attributeFQCN: Symfony\Component\Routing\Annotation\Route
  namespaces:
    - App\UserInterface\Web

commandsConfiguration:
  attributeFQCN: Symfony\Component\Console\Attribute\AsCommand
  namespaces:
    - App\UserInterface\CLI

doctrineEntitiesConfiguration:
  attributeFQCN: Doctrine\ORM\Mapping\Table
  namespaces:
    - App\Core\Models
    - Lib\Models
```

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
