# Level Map Syntax
Levels are created using characters, with each newline creating a new row on the level.
There are only a few characters:
- Orange (fragile) tiles: `!`
- White tiles: `@`
- Player position: `$` (max 1)
- Destination: `^` (max 1)
- Void padding: '` `' (a space)
- `[A-Za-z]`: Switches and bridge tiles.

Example map:
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
The rest of the level structure is in the form of switch and bridge attributes.
They control the behavior of switches (whether they're heavy or soft) and what they do.
You are allowed to make switches teleport the player, or toggle bridges, or turn them off (disappear/open) or on.

Switches are identified with uppercase letters (A-Z) and bridges use lowercase (a-z).
There's no connection between switches and bridges with the same letter unless you explicitly make it so.
You will use the cased characters in the level map to place them.

Example syntax, after or before the level map:
```
A: soft, on B, toggle a
B: hard, off b, teleport c d
C: soft
a: off
b: off
```
Here, the attribute names and arguments are case-insensitive. If you have `ON A`, it will turn
any bridge tiles `a` on.
The switches require a softness attribute, but don't need to have an action.
The bridges can also be given OFF/ON where OFF is no bridge.
If unspecified it's assumed to be ON, but you should be explicit in the level design.
Bridges are on by default since teleportation switches use the same scheme
and rely on the bridges being treated as regular tiles.
