package com.superhelix;

import java.util.ArrayList;
import java.util.List;

public class TileMetadata {
    private SwitchAttribute switchAttribute;
    private boolean startingBridgeState;
    private List<Position> positions;

    public TileMetadata() {
        // Just good defaults
        positions = new ArrayList<>();
        startingBridgeState = true;
    }
    public TileMetadata(SwitchAttribute switchAttribute, boolean startingBridgeStatus, List<Position> positions) {
        this.switchAttribute = switchAttribute;
        this.startingBridgeState = startingBridgeStatus;
        this.positions = positions;
    }

    public SwitchAttribute getSwitchAttribute() { return switchAttribute; }

    public boolean getStartingBridgeState() { return startingBridgeState; }

    public List<Position> getPositions() { return positions; }

    public void setSwitchAttribute(SwitchAttribute attr) { switchAttribute = attr; }

    public void setStartingBridgeState(boolean state) { startingBridgeState = state; }

    public void setPositions(List<Position> positions) { this.positions = positions; }
}
