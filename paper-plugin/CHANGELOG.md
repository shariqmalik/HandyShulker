# Paper plugin changelog

## [1.0.0-beta.1]

Initial beta release of the Paper compatibility plugins.

### Paper 26.3

- Add server-side shulker box item insertion and extraction for the unchanged official Handy Shulker client.
- Synchronize ender chest contents to the client for previews, including item data components.
- Follow the item selected by scrolling in the client tooltip.
- Enable custom interactions only after observing Handy Shulker's bundle-selection packet target a shulker box or ender chest. Scroll over one of these items once after joining to verify the client.
- Require Java 25.

### Legacy Paper

- Add basic server-side shulker box and ender chest item insertion and extraction.
- Require the `handyshulker.use` permission, disabled by default, because the legacy API build cannot inspect the client selection packet.
- Extract the first occupied container slot; scroll selection and ender chest preview synchronization are unavailable in this build.
- Target Paper 1.18.2 and newer with Java 17.

### Compatibility notes

- Players must install the official Handy Shulker Fabric client mod. No client changes are required.
- Match the Paper server version to the connecting Minecraft client version.
