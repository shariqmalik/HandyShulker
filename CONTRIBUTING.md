# Contributing

Thanks for helping improve Handy Shulker's Paper support. Contributions should focus on the Paper plugin modules unless a change is specifically intended for the upstream Fabric client mod.

## Before opening an issue

Include:

- Minecraft client version and Paper server version/build.
- Java version and plugin module (`paper-plugin` or `paper-plugin-26.3`).
- Steps to reproduce, expected behavior, and what happened instead.
- Relevant server log output, with private player or server details removed.

## Pull requests

1. Open an issue first for larger changes or changes to the client/server payload protocol.
2. Keep changes scoped to the relevant module. The 26.3 module uses Paper and Minecraft internals; the legacy module targets the Bukkit/Paper API.
3. Preserve compatibility with the official Handy Shulker client. Do not require a client patch or a forked client build for Paper support.
4. Build the module you changed using the command in its README and describe the versions/build command in your pull request.
5. Update documentation when requirements, supported versions, setup, or limitations change.
6. Do not commit generated build output, server files, credentials, or player data.

By submitting a contribution, you agree that it may be distributed under this repository's MIT License. Please retain the upstream Handy Shulker attribution when modifying or redistributing its Fabric mod or assets.

## Scope and attribution

The Fabric mod and original Handy Shulker logo come from [dfox288/HandyShulker](https://github.com/dfox288/HandyShulker) by Reza. This fork's Paper plugin code and server documentation are maintained here. Changes to the upstream Fabric client should be proposed to the original project.
