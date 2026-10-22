# Modrinth listing draft

Use this copy when creating the separate **Plugin** project for the Paper 26.3 build. This listing is for the Paper server plugin, not the upstream Fabric client mod.

## Project details

- **Title:** Handy Shulker Paper
- **Summary:** Paper server plugin support for the unchanged official Handy Shulker client mod.
- **Project type:** Plugin
- **Loader:** Paper
- **Minecraft version:** 26.3
- **Environment:** Server only
- **Java:** 25
- **License:** MIT
- **Icon:** [`handy-shulker-logo.png`](handy-shulker-logo.png), the original Handy Shulker icon by Reza.
- **Source:** https://github.com/shariqmalik/HandyShulker
- **Issues:** https://github.com/shariqmalik/HandyShulker/issues

## Description

Handy Shulker Paper lets players use the **official Handy Shulker Fabric client mod unchanged** on Paper 26.3. It handles shulker box item interactions and synchronizes the ender chest contents payload expected by the client, including item data components. The Paper 26.3 build also follows the item selected by scrolling in the client tooltip.

Players need the official Handy Shulker Fabric mod on their clients. This plugin is server-side and does not replace or modify that client mod. Requires Paper 26.3 and Java 25.

The Paper 26.3 plugin verifies clients from Handy Shulker's scroll-selection packet when the packet targets a shulker box or ender chest. Players should scroll over one of those items once before using click-to-insert. The separate legacy Paper build uses the `handyshulker.use` permission because its API-only implementation cannot inspect this packet.

Credits: the original Handy Shulker Fabric mod and logo are by Reza: https://modrinth.com/mod/handy-shulker. This is an independent Paper compatibility plugin maintained at https://github.com/shariqmalik/HandyShulker.

Some code and project text were created with AI assistance. Select the applicable AI generated content disclosure on the project before publishing.

## Release automation

The GitHub release workflow publishes the Paper 26.3 JAR to this project after the GitHub Release is created. Configure these repository settings:

- **Actions variable** `MODRINTH_PAPER_PROJECT_ID`: the new Modrinth plugin project's ID.
- **Actions secret** `MODRINTH_TOKEN`: a Modrinth personal access token with permission to create versions for the project.

The workflow publishes only the Paper 26.3 artifact to Modrinth. Both the 26.3 and legacy JARs remain attached to GitHub Releases.
