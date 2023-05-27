package com.superhelix;

import java.util.*;
//TODO:This will shit its pants if it a bridge is uppercase
public class StateNode {
    private final Player player;
    private final Map<Character, Boolean> bridgeStates;
    private final String identifier;
    private final StateNode parent;
    private final String moveDescription;

    public StateNode(Player player, StateNode parentNode, String moveDesc,
                     Map<Character, Boolean> bridgeStates) {
        this.player = player;
        this.bridgeStates = bridgeStates;
        parent = parentNode;
        moveDescription = moveDesc;

        StringBuilder states = new StringBuilder();
        // Iterate through the bridges alphabetically and add them to the string
        for (Iterator<Character> it = bridgeStates.keySet().stream().sorted().iterator(); it.hasNext(); ) {
            char key = it.next();
            boolean value = bridgeStates.get(key);
            states.append(value ? '1' : '0');
        }
        identifier = player.formatIdentifier() + "," + states;
    }

    /**
     * Generates every PlayerChange that is possible from the current position, orientation, and level state.
     * @return A list of PlayerChanges
     */
    private List<PlayerChange> generateNextPositions() {
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
    private Tile getTileOrVoid(List<List<Tile>> tiles, int x, int y) {
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
        List<List<Tile>> tiles = level.applyState(bridgeStates);

        for (PlayerChange change : generateNextPositions()) {
            Player newPlayer = change.player();

            // The PlayerChanges alone are not guaranteed to be valid, as some may fall off an edge or break a weak tile
            int x1 = newPlayer.getFirst().x();
            int y1 = newPlayer.getFirst().y();
            int x2 = newPlayer.getSecond().x();
            int y2 = newPlayer.getSecond().y();

            Tile tileA, tileB, tileC, tileD;
            tileB = getTileOrVoid(tiles, x1, y1);
            tileC = getTileOrVoid(tiles, x2, y2);

            // One fell off the platform
            if (newPlayer.isSplit()) {
                if (tileB == Tile.VOID || tileC == Tile.VOID)
                    continue;
            } else if (newPlayer.isVertical()) {
                // Would break a weak tile or fell off
                if (tileB == Tile.VOID || tileB == Tile.WEAK_FLOOR)
                    continue;
            } else {
                // i.e. ABCD where BC is the tile (horizontal)
                if (x1 != x2) {
                    tileA = getTileOrVoid(tiles, x1 - 1, y1);
                    tileD = getTileOrVoid(tiles, x2 + 1, y1);
                } else {
                    tileA = getTileOrVoid(tiles, x1, y1 - 1);
                    tileD = getTileOrVoid(tiles, x1, y2 + 1);
                }

                // discard if we've fallen off
                // this works since the block considers adjacent tiles in the long direction
                if (tileA == Tile.VOID && tileB == Tile.VOID
                        || tileB == Tile.VOID && tileC == Tile.VOID
                        || tileC == Tile.VOID && tileD == Tile.VOID)
                    continue;
            }

            // (x1, y1) & (x2, y2) should be valid
            // now, we need to register any switch events and create a new set of bridge states
            Map<Character, Boolean> newBridgeStates = new TreeMap<>(bridgeStates);
            for (Map.Entry<Character, TileMetadata> metadata : level.tilesMetadata().entrySet()) {
                // We know it's a switch if it has a switch attribute
                SwitchAttribute attr = metadata.getValue().getSwitchAttribute();
                if (attr != null) {
                    // We'll examine all the locations it is at to determine if we've stepped on a switch
                    List<Position> positions = level.tilesMetadata().get(metadata.getKey()).getPositions();
                    for (Position position : positions) {
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

                        // We can then invoke every action that the switch attribute contains
                        Position[] locations = attr.teleportLocations();
                        if (locations != null) {
                            newPlayer = new Player(
                                    locations[0],
                                    locations[1]
                            );
                        }

                        for (TileAction action : attr.bridgeActions()) {
                            char bridge = action.tileId();
                            BridgeEffect effect = action.effect();
                            boolean bridgeState;
                            switch (effect) {
                                case ON -> bridgeState = true;
                                case TOGGLE -> bridgeState = !newBridgeStates.get(bridge);
                                default -> bridgeState = false;
                            }
                            newBridgeStates.put(bridge, bridgeState);
                        }
                    }
                }
            }
            StateNode node = new StateNode(newPlayer, this, change.description(), newBridgeStates);
            children.add(node);
        }

        return children;
    }

    public Player getPlayer() { return player; }

    public StateNode getParent() { return parent; }

    public String getIdentifier() { return identifier; }

    public String getMoveDescription() { return moveDescription; }
}
