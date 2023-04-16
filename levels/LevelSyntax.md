# Level Map Syntax
Levels are created using characters, with each newline creating a new row on the level.
There are only a few characters:
- Orange (fragile) tiles: `!`
- White tiles: `@`
- Player position: `$` (up to 2)
- Destination: `^` (up to 2)
- Void padding: '` `' (a space)

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
A: SOFT, TOGGLE a
B: HARD, OFF b, TELEPORT c d
C: SOFT
a: OFF
b: OFF
```

Note that the switches require a softness attribute, but don't need to have an action.
The bridges can also be given OFF/ON where OFF is no bridge.
If unspecified it's assumed to be off, but you should be explicit in the level design.
