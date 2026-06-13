# IssuesFix

IssuesFix is a clean client-side Fabric mod for Minecraft `1.21.11` that repairs unstable player nametags without turning them into ESP.

It replaces only player nicknames, keeps normal Minecraft visibility rules, removes duplicated server/Lunar name layers, and rebuilds the visible name using the data already sent by the server.

## Features

- Player nametag replacement for broken or missing names
- Normal Minecraft font rendering with resource-pack compatibility
- Friendly player names turn green when they match your scoreboard team or detected faction tag
- Enemy or non-matching player names render red
- Server-provided HP and heart indicator after each player nickname, with health color by value
- Faction tags are read from scoreboard, player display data, and tablist names
- Faction role symbols are preserved: `#` leader, `+` member, `*` captain, `-` recruit
- Internal scoreboard team ids such as `[3playerA]` are ignored
- Player faction suffix renders after the health indicator in gray, for example `LTCFARM 20❤ [#LTC]`
- Compact name spacing with no extra scoreboard padding
- Shadow text enabled for the custom nametag
- Transparent nametag background
- Fully opaque nametag text
- No through-wall rendering
- Removes duplicated player nametags from Lunar name events and Text Display entities
- Does not hide world holograms, captions, or non-player labels
- Removes large TNT explosion smoke particles
- Removes Minecraft, Sodium, and Iris fog without changing chunk render distance
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

1. Download `issuesfix-1.3.0.jar` from the latest release.
2. Place it in your Fabric mods folder.
3. Start Minecraft.
4. Join a server and run `/issuefix` to verify the installation.

## Building

```powershell
.\gradlew.bat clean build
```

The production JAR is generated at:

```text
build/libs/issuesfix-1.3.0.jar
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
- `removeFog`: disables Minecraft fog
- `removeTntExplosionParticles`: removes large TNT explosion smoke particles
- `playerOutlineFix`: enables player outline cleanup
- `removeWhitePlayerOutlinesAutomatically`: removes unwanted white player outlines automatically
- `playerOutlineWorlds`: optional world filter for outline cleanup

## Project

- Repository: [Lopesnextgen/IssuesFix](https://github.com/Lopesnextgen/IssuesFix)
- Author: `Lopesnextgen`
- Credits: `Lopes (jvmexploit)`

## License

This project is released under the `CC0-1.0` license.
