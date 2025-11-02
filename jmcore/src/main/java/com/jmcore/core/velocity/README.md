# Internal Velocity System

This folder contains the internal velocity system for the plugin.

- Multiple sources can contribute velocity vectors for any entity each tick.
- All sources are summed for each entity and applied using `setVelocity`.
- Prevents sources from overwriting each other.
- Instantiate VelocityManager in your plugin’s main class.
- Use `VelocityManager#setVelocitySource` each tick for each source.
- Call `start()` on plugin enable and `stop()` on plugin disable.
- Other systems should call setVelocitySource each tick for their contributions.