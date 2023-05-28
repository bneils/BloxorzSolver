package com.superhelix;

import java.util.*;

public class StateNode {
    private final Player player;
    private final Map<Character, Boolean> states;
    private final String identifier;
    private final StateNode parent;
    private final String moveDescription;

    public StateNode(Player player, StateNode parentNode, String moveDesc,
                     Map<Character, Boolean> states) {
        this.player = player;
        this.states = states;
        parent = parentNode;
        moveDescription = moveDesc;
        identifier = player.formatIdentifier() + "," + concatBridgeStatesAlphabetically(states);
    }

    /**
     * Builds a binary string containing each bridge state in lexical order.
     * This can be thought of as a hash function for the bridgeStates map.
     * @param states A mapping containing each bridges' state
     * @return Binary pattern string
     */
    private static String concatBridgeStatesAlphabetically(Map<Character, Boolean> states) {
        StringBuilder binaryPattern = new StringBuilder();
        for (Iterator<Character> it = states.keySet().stream().sorted().iterator(); it.hasNext(); ) {
            char key = it.next();
            boolean value = states.get(key);
            binaryPattern.append(value ? '1' : '0');
        }
        return binaryPattern.toString();
    }

    /**
     * Generates every PlayerChange that is possible from the current position and orientation, regardless of outcome
     * @param player The player's current position
     * @return A list of PlayerChanges
     */
    private static List<PlayerChange> generateNextPositions(Player player) {
        List<PlayerChange> changes = new ArrayList<>();
        Direction[] directions = new Direction[]{Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};

        for (Direction direction : directions) {
            changes.add(new PlayerChange(player.newMovedPlayer(direction, false), direction.toString()));
            if (player.isSplit()) {
                changes.add(new PlayerChange(
                        player.newMovedPlayer(direction, true),
                        "SPACE " + direction));
            }
        }

        return changes;
    }

    /**
     * Gets a tile in the grid without worrying about bounds
     * @param tiles The grid of tiles
     * @param x The x coord
     * @param y The y coord
     * @return The tile at that coordinate, or void if it's out-of-bounds.
     */
    private static Tile getTileOrVoid(List<List<Tile>> tiles, int x, int y) {
        try {
            return tiles.get(y).get(x);
        } catch (IndexOutOfBoundsException e) {
            return Tile.VOID;
        }
    }

    /**
     * Builds a list of states that could be next after a single move at the current state without the player falling
     * @param level The level
     * @return A list of StateNodes
     */
    public List<StateNode> generateChildren(Level level) {
        List<StateNode> children = new ArrayList<>();
        List<List<Tile>> tiles = level.applyState(states);

        for (PlayerChange change : generateNextPositions(player)) {
            Player newPlayer = change.player();

            // The PlayerChanges alone are not guaranteed to be valid, as some may fall off an edge or break a weak tile
            int x1 = newPlayer.getFirst().x();
            int y1 = newPlayer.getFirst().y();
            int x2 = newPlayer.getSecond().x();
            int y2 = newPlayer.getSecond().y();

            Tile firstTile = getTileOrVoid(tiles, x1, y1);
            Tile secondTile = getTileOrVoid(tiles, x2, y2);

            if (newPlayer.isSplit()) {
                if (firstTile == Tile.VOID || secondTile == Tile.VOID)
                    continue;
            } else if (newPlayer.isVertical()) {
                // It should be noted that vertical slabs are the only victim of the weak floor tile
                if (firstTile == Tile.VOID || secondTile == Tile.WEAK_FLOOR)
                    continue;
            } else {
                // A-B-C-D is meant to represent the 4 relevant tiles that are coincident with the horizontal player
                // which are uniquely responsible for determining if the player is going to fall off
                Tile tileA, tileB, tileC, tileD;
                // This is legal since first and second tile are sorted as per Player's documentation
                tileB = firstTile;
                tileC = secondTile;

                if (x1 != x2) {
                    tileA = getTileOrVoid(tiles, x1 - 1, y1);
                    tileD = getTileOrVoid(tiles, x2 + 1, y1);
                } else {
                    tileA = getTileOrVoid(tiles, x1, y1 - 1);
                    tileD = getTileOrVoid(tiles, x1, y2 + 1);
                }

                // This makes more sense if you consider that the player has unlimited traction with the ground,
                // preventing it from slipping in a '@ @@' arrangement where the player is horizontal in the center.
                if (tileA == Tile.VOID && tileB == Tile.VOID
                        || tileB == Tile.VOID && tileC == Tile.VOID
                        || tileC == Tile.VOID && tileD == Tile.VOID)
                    continue;
            }

            Map<Character, Boolean> newTileStates = new TreeMap<>(states);
            for (Map.Entry<Character, TileMetadata> metadataEntry : level.tilesMetadata().entrySet()) {
                SwitchAttribute attr = metadataEntry.getValue().getSwitchAttribute();
                if (attr != null) {
                    for (Position position : metadataEntry.getValue().getPositions()) {
                        boolean activated;
                        int x = position.x();
                        int y = position.y();

                        boolean firstOnSwitch = (x1 == x && y1 == y);
                        boolean secondOnSwitch = (x2 == x && y2 == y);
                        activated = (attr.activationType() == ActivationType.SOFT) ?
                                firstOnSwitch || secondOnSwitch
                                : firstOnSwitch && secondOnSwitch;

                        if (!activated)
                            continue;

                        Position[] locations = attr.teleportLocations();
                        if (locations != null) {
                            newPlayer = new Player(
                                    locations[0],
                                    locations[1]
                            );
                        }

                        for (TileAction action : attr.bridgeActions()) {
                            boolean newBridgeState = switch (action.effect()) {
                                case ON -> true;
                                case TOGGLE -> !newTileStates.get(action.tileId());
                                default -> false; // OFF
                            };
                            newTileStates.put(action.tileId(), newBridgeState);
                        }
                    }
                }
            }
            children.add(new StateNode(newPlayer, this, change.description(), newTileStates));
        }

        return children;
    }

    public Player getPlayer() { return player; }

    public StateNode getParent() { return parent; }

    public String getIdentifier() { return identifier; }

    public String getMoveDescription() { return moveDescription; }
}
