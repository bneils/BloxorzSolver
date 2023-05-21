package com.superhelix;

import org.javatuples.Pair;

import java.util.*;

public class StateNode implements Cloneable {
    private final int distance;   // The number of moves traveled from start to be here
    private final Player player;
    private final Map<Character, Boolean> bridgeStates;
    private final String identifier;
    private final boolean isSplit;

    public StateNode(int distance, int x1, int y1, int x2, int y2, LinkedHashMap<Character, Boolean> bridgeStates, StateNode prev) {
        this.distance = distance;
        player = new Player(new Position(x1, y1), new Position(x2, y2));
        this.bridgeStates = bridgeStates;

        StringBuilder states = new StringBuilder();
        // bridgeStates must be a LinkedHashMap
        for (boolean state : bridgeStates.values()) {
            states.append(state ? "1" : "0");
        }
        identifier = player.formatIdentifier() + "," + states;
        isSplit = coordsAreSplit(x1, y1, x2, y2);
    }

    private boolean coordsAreSplit(int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        return !(dx == 0 && dy <= 1 || dx <= 1 && dy == 0);
    }

    public String getIdentifier() { return identifier; }

    private List<PlayerChange> generateNextPositions() {
        List<PlayerChange> possibleNext = new ArrayList<>();
        Direction[] directions = new Direction[]{Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};

        for (Direction direction : directions) {
            possibleNext.add(new PlayerChange(player.movedPlayer(direction, false), direction.toString()));
            if (player.isSplit()) {
                possibleNext.add(new PlayerChange(
                        player.movedPlayer(direction, true),
                        "SPACE " + direction));
            }
        }

        return possibleNext;
    }

    private Tile getCellOrVoid(ArrayList<ArrayList<Tile>> cells, int x, int y) {
        try {
            return cells.get(y).get(x);
        } catch (IndexOutOfBoundsException e) {
            return Tile.VOID;
        }
    }

    public ArrayList<Pair<StateNode, String>> generateChildren(Level level) {
        List<Pair<StateNode, String>> children = new ArrayList<>();
        List<List<Tile>> cells = level.applyState(bridgeStates);

        for (Pair<int[], String> posMove : generateNextPositions()) {
            int[] pos = posMove.getValue0();
            String move = posMove.getValue1();
            int x1 = pos[0], y1 = pos[1], x2 = pos[2], y2 = pos[3];

            Tile tileA, tileB, tileC, tileD;
            tileB = getCellOrVoid(cells, x1, y1);
            tileC = getCellOrVoid(cells, x2, y2);

            // filter this out if it's vertical and on a weak or void tile
            if (x1 == x2 && y1 == y2 && (tileB == Tile.VOID || tileB == Tile.WEAK_FLOOR))
                continue;

            // bad if one fell off the platform
            if (coordsAreSplit(x1, y1, x2, y2) && (tileB == Tile.VOID || tileC == Tile.VOID))
                continue;

            // i.e. ABCD where BC is the tile (horizontal)
            if (x1 != x2) {
                tileA = getCellOrVoid(cells, x1 - 1, y1);
                tileD = getCellOrVoid(cells, x2 + 1, y1);
            } else {
                tileA = getCellOrVoid(cells, x1, y1 - 1);
                tileD = getCellOrVoid(cells, x1, y2 + 1);
            }

            // discard if we've fallen off
            // this works since the block considers adjacent tiles in the long direction
            if (tileA == Tile.VOID && tileB == Tile.VOID
                    || tileC == Tile.VOID && tileD == Tile.VOID
                    || tileB == Tile.VOID && tileC == Tile.VOID)
                continue;

            // (x1, y1) & (x2, y2) should be valid
            // now, we need to register any switch events and create a new set of bridge states
            Map<Character, Boolean> newBridgeStates = new TreeMap<>(bridgeStates);
            for (Map.Entry<Character, SwitchAttribute> switchElement : level.switchAttributes.entrySet()) {
                List<Position> positions = level.elementPositions.get(switchElement.getKey());
                for (Position position : positions) {
                    boolean activated;
                    int x = position.x();
                    int y = position.y();
                    if (switchElement.getValue().getActivationType() == ActivationType.SOFT) {
                        activated = (x1 == x && y1 == y) || (x2 == x && y2 == y);
                    } else {
                        activated = (x1 == x && y1 == y) && (x1 == x2 && y1 == y2);
                    }

                    if (!activated)
                        continue;

                    // this switch is activated, state needs to change

                    char[] tpLocs = switchElement.getValue().getTeleportLocations();
                    if (tpLocs != null) {
                        // simply change the location if we're teleporting
                        List<Position> ptA = level.elementPositions.get(tpLocs[0]);
                        List<Position> ptB = level.elementPositions.get(tpLocs[1]);

                        if (ptA.size() == 1 && ptB.size() == 1) {
                            x1 = ptA.get(0).x();
                            y1 = ptA.get(0).y();
                            x2 = ptB.get(0).x();
                            y2 = ptB.get(0).y();
                        }
                    }

                    List<BridgeAction> actions = switchElement.getValue().getBridgeActions();
                    for (BridgeAction action : actions) {
                        char bridge = action.bridgeId();
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

            StateNode node = new StateNode(distance + 1, x1, y1, x2, y2, newBridgeStates, this);
            children.add(new Pair<>(node, move));
        }

        return children;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public int getDistance() { return distance; }

    public int[] getPlayerPosition() { return new int[] { x1, y1, x2, y2 }; }
}
