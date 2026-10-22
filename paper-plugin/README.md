# HandyShulker Paper plugin: older servers

This Java 17 build is for Paper 1.18.2 and newer. It handles shulker and ender chest item insert/extract clicks server-side for clients whose reported brand includes `Fabric`. Vanilla-branded clients keep normal inventory behavior. Paper exposes the client brand rather than its installed mod list, so this gate cannot distinguish Handy Shulker from other Fabric clients. Since the legacy API does not expose the mod's selection packet, extraction defaults to the first occupied slot.

Build from the repository root:

```powershell
.\gradlew.bat -p paper-plugin build
```

Install `paper-plugin/build/libs/handy-shulker-paper-1.0.0.jar` in the server's `plugins` folder.

The older-server build does not send the mod's ender chest preview payload. For Minecraft 26.3, use the full-sync build documented in [paper-plugin-26.3](../paper-plugin-26.3/README.md).

The client mod currently targets Minecraft 26.3. A plugin that loads on older Paper does not make a 26.3 client protocol-compatible with an older Minecraft server; matching game versions or a compatible protocol bridge are still required.
