# HandyShulker Paper plugin for Minecraft 26.3

This build sends the exact `handyshulker:sync_ender_chest` payload expected by the official client. It uses Minecraft 26.3's registry-aware `ItemStack` stream codec, so the client receives normal network item stacks, including their data components. No client changes are needed. It also observes the vanilla bundle-selection packet the client reuses for scroll selection, so extraction follows the item highlighted in the tooltip.

The plugin verifies the client by observing Handy Shulker's `ServerboundSelectBundleItemPacket` while its target slot contains a shulker box or ender chest. It enables the interactions and sends the initial ender chest snapshot after that verification. Players should scroll over a shulker box or ender chest once before using click-to-insert. Ordinary vanilla bundle selection does not enable the plugin because its target slot is not a shulker or ender chest.

After verification, the plugin syncs the ender chest snapshot after insert/extract interactions and inventory screens close (including a placed ender chest). It also supports shulker box interactions. No client changes are needed.

Build from the repository root:

```powershell
.\gradlew.bat -p paper-plugin-26.3 build
```

Install `paper-plugin-26.3/build/libs/handy-shulker-paper-1.0.0-26.3.jar` in the Paper 26.3 `plugins` folder. This build requires Java 25 and Paper 26.3. For older Paper versions, build and use the Java 17 artifact from [paper-plugin](../paper-plugin/README.md); that legacy artifact cannot sync ender chest previews.

This adapter uses Paper/Minecraft internals to observe the selection packet and encode the client payload. Rebuild and check it when Paper/Minecraft changes its network pipeline or item stack codec.
