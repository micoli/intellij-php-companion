<!-- Plugin description -->
# PHP Companion Plugin

A PhpStorm/IntelliJ plugin that enhances PHP development workflow with advanced navigation and code analysis features, particularly focused on Symfony Messenger pattern and peer navigation capabilities.

## Features

### 🚀 PHP Companion
- **Smart Navigation**: Navigate from message dispatch calls directly to their corresponding handlers
- **Find Usages**: Discover all dispatch calls for a specific message class or handler method
- **Message Detection**: Automatically identify message classes based on naming patterns and interfaces
- **Handler Detection**: Recognize message handlers through interfaces, attributes, or naming conventions
- **Multi-dispatch Support**: Support for various dispatch method names (`dispatch`, `query`, `command`, `handle`)

### 🔗 Peer Navigation (code to test to code navigation)
- **Pattern-based Navigation**: Navigate between related classes using regex patterns
- **Flexible Mapping**: Define custom source-to-target class relationships
- **Go to Declaration**: Jump between peer classes with a single keystroke

### ⚙️ Configuration Management
- **Hot Reload**: Configuration changes are automatically detected and applied
- **Multiple Formats**: Support for JSON and YAML configuration files
- **Hierarchical Config**: Local configuration files can override global settings
- **Real-time Notifications**: Get notified when configuration is loaded or encounters errors

## Configuration

The plugin uses configuration files placed in your project root. The plugin will automatically detect and load configuration from any of these files (in order of precedence):

- `.php-companion.local.yaml`
- `.php-companion.local.json`
- `.php-companion.yaml`
- `.php-companion.json`

### Configuration Structure

#### Complete Configuration Example

```json
{
  "symfonyMessenger": {
    "projectRootNamespace": "\\App",
    "messageClassNamePatterns": ".*(Message|Command|Query|Event|Input)$",
    "messageInterfaces": [
      "App\\Shared\\Application\\Message\\MessageInterface"
    ],
    "messageHandlerInterfaces": [
      "Symfony\\Component\\Messenger\\Handler\\MessageHandlerInterface"
    ],
    "dispatchMethods": [
      "dispatch",
      "query", 
      "command",
      "handle"
    ],
    "handlerMethods": [
      "__invoke",
      "handle"
    ]
  },
  "peerNavigation": {
    "peers": [
      {
        "source": "\\\\App\\\\Application\\\\(.+)\\\\Command\\\\(.+)Command",
        "target": "\\\\App\\\\Application\\\\$1\\\\CommandHandler\\\\$2CommandHandler"
      },
      {
        "source": "\\\\App\\\\Application\\\\(.+)\\\\Query\\\\(.+)Query", 
        "target": "\\\\App\\\\Application\\\\$1\\\\QueryHandler\\\\$2QueryHandler"
      }
    ]
  }
}
```

#### YAML Configuration Example

```yaml
symfonyMessenger:
  projectRootNamespace: "\\App"
  messageClassNamePatterns: ".*(Message|Command|Query|Event|Input)$"
  messageInterfaces:
    - "App\\Shared\\Application\\Message\\MessageInterface"
  messageHandlerInterfaces:
    - "Symfony\\Component\\Messenger\\Handler\\MessageHandlerInterface"
  dispatchMethods:
    - "dispatch"
    - "query"
    - "command" 
    - "handle"
  handlerMethods:
    - "__invoke"
    - "handle"

peerNavigation:
  peers:
    - source: "\\\\App\\\\Application\\\\(.+)\\\\Command\\\\(.+)Command"
      target: "\\\\App\\\\Application\\\\$1\\\\CommandHandler\\\\$2CommandHandler"
    - source: "\\\\App\\\\Application\\\\(.+)\\\\Query\\\\(.+)Query"
      target: "\\\\App\\\\Application\\\\$1\\\\QueryHandler\\\\$2QueryHandler"
```

### Symfony Messenger Configuration

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `projectRootNamespace` | string | `"\\App"` | Root namespace for scanning classes |
| `messageClassNamePatterns` | string | `".*(Message\|Command\|Query\|Event\|Input)$"` | Regex pattern to identify message classes |
| `messageInterfaces` | string[] | `[]` | Interfaces that message classes implement |
| `messageHandlerInterfaces` | string[] | `["Symfony\\Component\\Messenger\\Handler\\MessageHandlerInterface"]` | Interfaces that handler classes implement |
| `dispatchMethods` | string[] | `["dispatch", "query", "command", "handle"]` | Method names used to dispatch messages |
| `handlerMethods` | string[] | `["__invoke", "handle"]` | Method names in handler classes |

### Peer Navigation Configuration

| Property | Type | Description |
|----------|------|-------------|
| `peers` | object[] | Array of navigation rules |
| `peers[].source` | string | Regex pattern matching source class FQN (use double backslashes for namespace separators) |
| `peers[].target` | string | Target class FQN pattern with `$1`, `$2`, etc. for regex group substitution |

#### Peer Navigation Examples

**Navigate from Commands to CommandHandlers:**
```json
{
  "source": "\\\\App\\\\Application\\\\(.+)\\\\Command\\\\(.+)Command",
  "target": "\\\\App\\\\Application\\\\$1\\\\CommandHandler\\\\$2CommandHandler"
}
```

**Navigate from Entities to Repositories:**
```json
{
  "source": "\\\\App\\\\Domain\\\\(.+)\\\\Entity\\\\(.+)",
  "target": "\\\\App\\\\Infrastructure\\\\Repository\\\\$2Repository"
}
```

**Navigate from Controllers to Services:**
```json
{
  "source": "\\\\App\\\\Controller\\\\(.+)Controller",
  "target": "\\\\App\\\\Service\\\\$1Service"
}
```

### Configuration Tips

1. **Regex Escaping**: In JSON configuration, use double backslashes (`\\\\`) for namespace separators in regex patterns
2. **Local Overrides**: Use `.php-companion.local.*` files for project-specific settings that shouldn't be committed
3. **Hot Reload**: The plugin checks for configuration changes every 2 seconds
4. **Error Handling**: Configuration errors will be displayed as notifications in the IDE

<!-- Plugin description end -->

## Development

### Prerequisites

- Java 11+
- IntelliJ IDEA with Plugin DevKit
- Gradle

### Project Structure

```
src/main/java/org/micoli/php/
├── configuration/           # Configuration management
│   ├── models/             # Configuration data models
│   ├── ConfigurationFactory.java
│   └── GsonTools.java      # JSON merging utilities
├── peerNavigation/         # Peer navigation feature
│   ├── configuration/      # Peer navigation config models
│   └── navigation/         # Navigation handlers
├── symfony/messenger/      # Symfony Messenger support
│   ├── configuration/      # Messenger config models
│   ├── navigation/         # Go-to-declaration handlers
│   ├── service/           # Core messenger services
│   └── usage/             # Find usages handlers
├── ui/                    # UI utilities
└── MessengerProjectComponent.java  # Main project component
```

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