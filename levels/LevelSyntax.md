# Level Map Syntax
Levels are created using characters, with each newline creating a new row on the level.
There are only a few characters:
- Orange (fragile) tiles: `!`
- White tiles: `@`
- Player position: `$` (max 1)
- Destination: `^` (max 1)
- Void padding: '` `' (a space)
- `[A-Za-z]`: Switches and bridge tiles.

Example map in a `.txt` file.
```
@@@
@$@@@@
@@@@@@@@@
 @@@@@@@@@
     @@^@@
      @@@
```

Note that you can only use spaces - so no tabs. Padding is only required to make
the indentation equal or to insert a gap in a level, like between platforms.
The rest of the level structure is in the form of switch and bridge attributes in a separate file.
They control the behavior of switches (whether they're heavy or soft) and what they do.
You are allowed to make switches teleport the player, or toggle bridges, or turn them off (disappear/open) or on.

Switches can be conventionally be identified with uppercase letters (A-Z) and bridges use lowercase (a-z), but it's
not required.
Note that the identifiers are case-sensitive.
You will use the characters in the level map to place them.

Example syntax, in a separate `.info` file.
```
A: soft, on B, toggle a
B: hard, off b, teleport c d
C: soft
a: off
b: off
```
Switches default to being 'soft' and all tiles default to being 'on' or solid.
You must explicitly make a tile 'off' for it to start out that way.
The order of the arguments for the teleport verb matter, since the first location will be where the player controller is placed.
This is important because the program will assume the controller moves the first parameter, and that saying SPACE will toggle that control.