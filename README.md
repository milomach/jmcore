# JMCore

JMCore is a low-level API plugin for Minecraft: Java Edition (Paper), providing flexible building blocks for plugin developers to create their own fully custom systems intended to support game-like development without being constrained by opinionated, ready-made solutions.

## Purpose

JMCore is intended to serve as a foundation for other plugins rather than as a complete gameplay solution. It provides low-level functionality that developers can build upon while retaining control over how their own systems are designed and implemented.

JMCore does not aim to provide complete, user-facing systems. Instead, it focuses on providing the underlying capabilities needed to develop them.

## Intended Audience

JMCore is primarily intended for **plugin developers** building custom gameplay or game-like experiences on Paper.

It is not intended to be a ready-to-use plugin for server owners or players. A typical use case is for another plugin to depend on JMCore and use its APIs as part of its own implementation.

## Development Status

JMCore is currently in **paused** development and is not yet considered stable.

The API and internal implementation may change as development continues. Documentation and examples will be added as the project develops.

There is currently no planned release date for a stable version.

## Getting Started

### Requirements

- Minecraft: Java Edition 1.21.10
- Paper
- Java 21 or later

### Installation

JMCore is installed as a server plugin and is intended to be used as a dependency by other Paper plugins.

1. Place `JMCore.jar` in the server's `plugins` directory.
2. Start or restart the server.
3. Declare JMCore as a dependency of your plugin.
4. Use the APIs provided by JMCore in your plugin.

API documentation and usage examples are currently a work in progress.

## Contributing

JMCore is a personal project, but contributions, suggestions, and bug reports are welcome.

- Open an issue for bugs, suggestions, or questions.
- Submit a pull request for fixes or improvements.
- See `CONTRIBUTING.md` for contribution guidelines.

## License

JMCore is licensed under the **AGPL-3.0** license.

See `LICENSE.md` for the complete license terms.

## Additional Files

- `ACKNOWLEDGEMENTS.md` — third-party credits and attributions
- `TODO.md` — ongoing tasks and development plans
- `CONTRIBUTING.md` — contribution guidelines
- `LICENSE.md` — license terms