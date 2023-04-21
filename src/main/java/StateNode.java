import org.javatuples.Pair;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class StateNode implements Cloneable {
    private final int distance;   // The number of moves traveled from start to be here
    private final int x1, y1, x2, y2;     // The player's position
    private final Map<Character, Boolean> bridgeStates;
    private final StateNode previous;
    private ArrayList<StateNode> next;
    private final String identifier;
    private final boolean isSplit;

    public StateNode(int distance, int x1, int y1, int x2, int y2, Map<Character, Boolean> bridgeStates, StateNode prev) {
        this.distance = distance;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.bridgeStates = bridgeStates;
        previous = prev;
        next = new ArrayList<>();

        String loc = "%d,%d,%d,%d".formatted(x1, y1, x2, y2);
        StringBuilder states = new StringBuilder();
        for (Object k : bridgeStates.keySet().stream().sorted().toArray()) {
            String bit = bridgeStates.get((Character) k) ? "1" : "0";
            states.append(bit);
        }
        identifier = loc + states;
        isSplit = coordsAreSplit(x1, y1, x2, y2);
    }

    private boolean coordsAreSplit(int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        return !(dx == 0 && dy <= 1 || dx <= 1 && dy == 0);
    }

    public String getIdentifier() { return identifier; }

    private ArrayList<int[]> generateNextPositions() {
        ArrayList<int[]> possibleNext = new ArrayList<>();
        int[][] offsets = new int[][] { {-1, 0}, {1, 0}, {0, -1}, {0, 1} };

        for (int[] offset : offsets) {
            int i = offset[0], j = offset[1];
            if (isSplit) {
                possibleNext.add(new int[]{x1 + j, y1 + i, x2, y2});
                possibleNext.add(new int[]{x1, y1, x2 + j, y2 + i});
            } else {
                int x1_next = x1;
                int y1_next = y1;
                int x2_next = x2;
                int y2_next = y2;

                if (x1 == x2 && y1 == y2) {
                    // if it's vertical each next state is horizontal
                    if (j != 0) {
                        x1_next = (j == 1) ? x1 + 1 : x1 - 2;
                        x2_next = x1_next + 1;
                    } else {
                        y1_next = (i == 1) ? y1 + 1 : y1 - 2;
                        y2_next = y1_next + 1;
                    }
                } else {
                    if (j != 0) { // left, right
                        if (x1 != x2) {
                            // <- ## ->
                            x2_next = x1_next = ((j > 0) ? x2 : x1) + j;
                        } else {
                            // <- # ->
                            // <- # ->
                            x2_next = x1_next = x1 + j;
                        }
                    } else if (i != 0) { // up, down
                        if (x1 != x2) {
                            // ^^
                            // ##
                            // vv
                            y2_next = y1_next = y1 + i;
                        } else {
                            // ^
                            // #
                            // #
                            // v
                            y1_next = y2_next = ((i > 0) ? y2 : y1) + i;
                        }
                    }
                }

                possibleNext.add(new int[]{x1_next, y1_next, x2_next, y2_next});
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

    public ArrayList<StateNode> generateChildren(Level level) {
        ArrayList<StateNode> children = new ArrayList<>();
        ArrayList<ArrayList<Tile>> cells = level.applyState(bridgeStates);

        for (int[] pos : generateNextPositions()) {
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
                ArrayList<Pair<Integer, Integer>> positions = level.elementPositions.get(switchElement.getKey());
                for (Pair<Integer, Integer> position : positions) {
                    boolean activated;
                    int x = position.getValue0();
                    int y = position.getValue1();
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
                        ArrayList<Pair<Integer, Integer>> ptA = level.elementPositions.get(tpLocs[0]);
                        ArrayList<Pair<Integer, Integer>> ptB = level.elementPositions.get(tpLocs[1]);

                        if (ptA.size() == 1 && ptB.size() == 1) {
                            x1 = ptA.get(0).getValue0();
                            y1 = ptA.get(0).getValue1();
                            x2 = ptB.get(0).getValue0();
                            y2 = ptB.get(0).getValue1();
                        }
                    }

                    ArrayList<BridgeAction> actions = switchElement.getValue().getBridgeActions();
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
            children.add(node);
        }

        return children;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /// Returns whether the two player blocks are connected
    public boolean isSplit() { return isSplit; }
}
