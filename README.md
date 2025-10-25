## ⚙️ What is JMCore?

- **JMCore** is a standalone utility framework plugin for Minecraft: Java Edition (Paper), designed to serve as a foundation for fully custom *game-like* development.
- JMCore provides a **public runtime API**, accessible to other Paper plugins that declare it as a dependency.
- JMCore is unopinionated, modular, and performant — handling the low-level complexity so you can focus on **creating**.
---
## ✨ Key Features

- 🎨 **UI / Screen Effects** — Build display-entity-based interfaces and custom interaction layers.
- 🧱 **Models / Animations** — Drive dynamic entity visuals with a fully featured rig animation system.
- 🎮 **Player Input** — Capture all forms of player input, including motion, look, inventory/hotbar actions, and more through unified input events.
- 🌀 **Velocity System** — Control motion and physics with precise velocity management.
- 🧍 **NPCs** — Create and manage pathfinding, targeting, and controlled AI behaviors.
- 🧩 **Interactions** — Handle hitboxes, hurtboxes, and raycasting for custom collision logic.
---
## 🧠 Philosophy

> **“Give developers the tools, not the limitations.”**

- JMCore provides *fundamental systems*, not prebuilt features, enabling you to design custom gameplay mechanics your own way.
- It handles the complex, low-level workarounds required to go beyond Minecraft’s built-in systems, while leaving full creative control in your hands.
---
## 👥 Who It's For

- **Plugin Developers** who want to build custom gameplay systems without constantly fighting Minecraft’s built-in limitations.
- Projects that rely on **Paper’s plugin API** and want **deep engine-level functionality** without sacrificing modularity or control.

	JMCore is **not** a plugin for end users — it’s a **developer library** that other plugins can utilize.
---
## ⚠️ Notes & Considerations

- JMCore’s systems often **override** or **replace** vanilla Minecraft mechanics.
- Some components are only intended for **fully custom gameplay** environments.
- While modular, certain systems have **necessary interdependencies** to function optimally.

	Use JMCore as a **foundation**, not an add-on — it’s built for total creative control.
---
## 🛠️ Development Status

- JMCore is in **active development** and is **not yet feature-stable**.
- Expect ongoing refactors, optimizations, and API adjustments until the first stable release.
- There is currently **no ETA** for a production version.

	Early adopters and contributors are welcome to experiment and provide feedback!
---
## 🤝 Contributing

Contributions, ideas, and bug reports are always welcome! 

You can:
- Open issues for bugs, ideas, or questions
- Submit pull requests for fixes and improvements
- Join community discussions once public links are available

	Please see `CONTRIBUTING.md` for contribution guidelines (coming soon).
---
## 📜 License

Licensed under the **AGPL-3.0** License.
- JMCore is **free and open source**.
- If you distribute a plugin that modifies or extends JMCore, it must also be **open-source under an AGPL-compatible license**.
- This ensures that JMCore, and all of its derivatives, remain open and benefit the entire developer community.

	Please see `LICENSE.md` for details.
---
## 🌍 Links

- 📦 **[GitHub Repository](https://github.com/milomach/jmcore)**
- 💬 **[Discord](https://discord.gg/TdBhmS4suY)**
- 🧭 **Documentation:** *(TODO)*
---
## 🚀 Getting Started

**Requirements**
- Minecraft Java **1.21.10** (Paper)
- Java **21+**
- _(Additional dependencies TBD)_

**Installation**
1. Place `JMCore.jar` into your server’s `plugins` folder.
2. Restart the server to generate the configuration and API hooks.
3. Add `JMCore` as a dependency in your plugin’s `plugin.yml` or build configuration.
4. Access JMCore’s API via the provided Java interfaces.

	*(API documentation and code examples coming soon.)*
---
## 🧾 Additional Files

- `ACKNOWLEDGEMENTS.md` – credits and third-party attributions
- `TODO.md` – roadmap and ongoing tasks
- `CONTRIBUTING.md` – contribution guidelines
- `LICENSE.md` – AGPL-3.0 license terms
