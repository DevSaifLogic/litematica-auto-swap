# Litematica AutoSwap

Fabric client mod that selects the inventory item matching the block in the targeted Litematica schematic. It can run while the configured key is held or continuously when immediate mode is enabled. By default it pauses while holding a tool and displays a message if the required block is missing.

## Requirements

- Minecraft 26.2
- Fabric Loader
- Fabric API
- Litematica and MaLiLib
- Java 25 or newer

Press **X** to activate swapping and **C** to open the configuration screen. Key bindings can be changed in Minecraft's Controls menu. The `autoswap.json` configuration is created in the game config directory.

## Build

```powershell
.\gradlew.bat build
```

Install the jar from `build\libs` in a compatible Fabric client with Litematica installed.

## License

See [LICENSE](LICENSE).
