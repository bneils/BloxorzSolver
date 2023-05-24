package com.superhelix;

import java.util.ArrayList;
import java.util.List;

public class TileMetadata {
    private SwitchAttribute switchAttribute;
    private boolean startingBridgeState;
    private List<Position> positions;

    private final char id;

    public TileMetadata(char c) {
        // Just good defaults
        positions = new ArrayList<>();
        startingBridgeState = true;
        id = c;
    }
    public TileMetadata(char c, SwitchAttribute switchAttribute, boolean startingBridgeStatus, List<Position> positions) {
        this.switchAttribute = switchAttribute;
        this.startingBridgeState = startingBridgeStatus;
        this.positions = positions;
        id = c;
    }

    public boolean isBridge() {
        return Character.isLowerCase(id);
    }

    public SwitchAttribute getSwitchAttribute() { return switchAttribute; }

    public boolean getStartingBridgeState() { return startingBridgeState; }

    public List<Position> getPositions() { return positions; }

    public void setSwitchAttribute(SwitchAttribute attr) { switchAttribute = attr; }

    public void setStartingBridgeState(boolean state) { startingBridgeState = state; }

    public void setPositions(List<Position> positions) { this.positions = positions; }
}
