# IssuesFix

IssuesFix is a clean client-side Fabric mod for Minecraft `1.21.11` that repairs unstable player nametags without turning them into ESP.

It replaces only player nicknames, keeps normal Minecraft visibility rules, and rebuilds the visible name using the server-provided scoreboard/team information.

## Features

- Player nametag replacement for broken or missing names
- Normal Minecraft font rendering with resource-pack compatibility
- Friendly player names turn green when they match your scoreboard team or detected faction tag
- Enemy or non-matching player names render red
- Faction/team suffix renders after the nickname in gray
- Compact name spacing with no extra scoreboard padding
- Shadow text enabled for the custom nametag
- Transparent nametag background
- Fully opaque nametag text
- No through-wall rendering
- Does not hide world holograms, display entities, captions, or non-player labels
- Optional player white-outline cleanup
- `/issuefix` chat status with GitHub release update checking

## Command

Show the release status in chat:

```text
/issuefix
```

The chat response shows:

- `Status`: GitHub release checker health
- `Version`: local mod version
- `Is updated?`: current release status
- `Author`: GitHub release author

If a newer release is available, click the underlined update line to open the latest GitHub release page.

## Requirements

| Dependency | Version |
| --- | --- |
| Minecraft | `1.21.11` |
| Java | `21` or newer |
| Fabric Loader | `0.19.2` or newer |

## Installation

1. Download `issuesfix-1.0.0.jar` from the latest release.
2. Place it in your Fabric mods folder.
3. Start Minecraft.
4. Join a server and run `/issuefix` to verify the installation.

## Building

```powershell
.\gradlew.bat clean build
```

The production JAR is generated at:

```text
build/libs/issuesfix-1.0.0.jar
```

## Configuration

IssuesFix stores its config at:

```text
config/issuesfix.json
```

The default configuration is designed for normal gameplay. It only replaces player nametags and keeps server world text untouched.

Useful options:

- `customNametags`: enables the repaired player nametag renderer
- `customNametagBackground`: toggles the vanilla nametag background
- `customNametagShadow`: toggles shadow text
- `playerOutlineFix`: enables player outline cleanup
- `removeWhitePlayerOutlinesAutomatically`: removes unwanted white player outlines automatically
- `playerOutlineWorlds`: optional world filter for outline cleanup

## Project

- Repository: [Lopesnextgen/IssuesFix](https://github.com/Lopesnextgen/IssuesFix)
- Author: `Lopesnextgen`
- Credits: `Lopes (jvmexploit)`

## License

This project is released under the `CC0-1.0` license.
