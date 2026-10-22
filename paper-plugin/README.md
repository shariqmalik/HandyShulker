# HandyShulker Paper plugin: older servers

This Java 17 build is for Paper 1.18.2 and newer. The Bukkit/Paper API baseline cannot observe Handy Shulker's selection packet, so interactions are disabled by default and require the `handyshulker.use` permission. Grant it only to players using the official client mod. For example, with LuckPerms: `/lp user <player> permission set handyshulker.use true`. Extraction defaults to the first occupied slot because this build cannot read the mod's scroll selection.

Build from the repository root:

```powershell
.\gradlew.bat -p paper-plugin build
```

Install `paper-plugin/build/libs/handy-shulker-paper-1.0.0.jar` in the server's `plugins` folder.

The older-server build does not send the mod's ender chest preview payload. For Minecraft 26.3, use the full-sync build documented in [paper-plugin-26.3](../paper-plugin-26.3/README.md).

The client mod currently targets Minecraft 26.3. A plugin that loads on older Paper does not make a 26.3 client protocol-compatible with an older Minecraft server; matching game versions or a compatible protocol bridge are still required.
