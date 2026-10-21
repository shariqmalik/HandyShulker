# Handy Shulker — Paper server support

<p align="center">
  <img src="docs/handy-shulker-logo.png" alt="Handy Shulker logo" width="220">
</p>

<p align="center">
  <a href="https://github.com/dfox288/HandyShulker">Original Handy Shulker Fabric mod</a>
  · Paper plugins maintained in this fork
</p>

Handy Shulker lets players insert items into shulker boxes, preview their contents, and extract a selected item without placing the box. This fork adds Paper server plugins so players can keep using the **official Handy Shulker client mod unchanged** on Paper.

## Paper compatibility

| Server | Plugin build | Java | Notes |
| --- | --- | --- | --- |
| Paper 26.3 | [`paper-plugin-26.3`](paper-plugin-26.3/README.md) | 25 | Selection-aware extraction, shulker support, and ender chest payload synchronization for the official client. |
| Paper 1.18.2 and newer legacy API | [`paper-plugin`](paper-plugin/README.md) | 17 | Basic shulker and ender chest item insertion/extraction. It cannot send the 26.3 ender chest preview payload or read scroll selection. |

Use a server version compatible with the Minecraft client. A plugin that loads on an older Paper release does not make a 26.3 client compatible with an older game protocol.

## Install

1. Install the official [Handy Shulker Fabric mod](https://modrinth.com/mod/handy-shulker) and its required Fabric dependencies on each client.
2. Download or build the plugin variant matching your Paper server from the table above.
3. Put the plugin JAR in the server's `plugins` folder and restart the server.
4. Players continue to use the official client mod; no client-side fork or configuration change is required.

## Build

Build the Paper 26.3 plugin (requires Java 25):

```powershell
.\gradlew.bat -p paper-plugin-26.3 build
```

Build the legacy Paper plugin (requires Java 17):

```powershell
.\gradlew.bat -p paper-plugin build
```

The JARs are written to each module's `build/libs/` directory. See the module READMEs for details and limitations.

## Releases and pull requests

Pull requests and branch pushes build both Paper plugins (as well as the Fabric mod) in GitHub Actions. The generated JARs are available as workflow artifacts.

To publish a Paper release, set the `version` in both Paper module `build.gradle` files to the same release version, commit that change, and push a tag named `paper-v<version>` (for example, `paper-v1.0.0`). The workflow builds both plugins and attaches their JARs to a GitHub Release. The tag must match both module versions.

GitHub Releases contain both plugin builds. The release workflow can also publish the Paper 26.3 JAR to a separate Modrinth plugin project once `MODRINTH_PAPER_PROJECT_ID` and `MODRINTH_TOKEN` are configured. See the [Modrinth listing draft](docs/MODRINTH_PROJECT.md). Modrinth's rules require separate projects for files with different functionality; the Paper plugin is separate from the upstream Fabric client mod. See [Modrinth's project rules](https://modrinth.com/legal/rules/).

The original Fabric mod can be built separately with `./gradlew build` and its normal Fabric toolchain.

## Contributing

Bug reports and pull requests are welcome. Read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a change. Please identify the exact Minecraft and Paper versions involved, especially for packet, payload, and inventory behavior.

## Credits

The Fabric client mod, its design, and the included Handy Shulker logo are by **Reza**, in the upstream [dfox288/HandyShulker](https://github.com/dfox288/HandyShulker) project. This repository is a fork that adds and maintains Paper server compatibility. The upstream project is also available on [Modrinth](https://modrinth.com/mod/handy-shulker).

The upstream project and this fork are distributed under the MIT License. See [LICENSE](LICENSE).
