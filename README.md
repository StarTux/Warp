# Warp

Provide warps. Warps are stored locations accessible to players so they can teleport to them via command. Each warp has a name.

## Compatibility

Made for Paper 1.15.2, compatible with 1.16.2; probably future versions. Possibly Spigot.

## Commands

- `/warp` List all warps or teleport to one.
- `/setwarp` Set a warp at the current location (admin only)

## Permissions

- `warp.warp` Use `/warp`
- `warp.setwarp` Use `/setwarp`

## Data storage

The plugin uses a flat json file to save all warps in. They are saved whenever a new warp is created.

## Future directions

- Delete/modify warps via command
- Command permissions
- GUI