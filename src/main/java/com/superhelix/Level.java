package com.superhelix;

import java.util.*;

import org.javatuples.Pair;

// Contains the original level
public record Level(
        List<List<Tile>> tiles,
        Position playerPos,
        Position goalPos,
        Map<Character, SwitchAttribute> switchAttributes,
        Map<Character, Boolean> bridgeInitialStatuses,
        Map<Character, List<Position>> elementPositions
) {
    public List<List<Tile>> applyState(Map<Character, Boolean> states) {
        // Do a weird clone of cells
        List<List<Tile>> newCells = new ArrayList<>();
        for (List<Tile> row : tiles) {
            List<Tile> newRow = new ArrayList<>(row.size());
            newRow.addAll(row);
            newCells.add(newRow);
        }

        // Apply each state to bridge locations
        for (Map.Entry<Character, Boolean> entry : states.entrySet()) {
            Tile newTile = entry.getValue() ? Tile.STRONG_FLOOR : Tile.VOID;
            for (Position pos : elementPositions.get(entry.getKey())) {
                newCells.get(pos.y()).set(pos.x(), newTile);
            }
        }

        return newCells;
    }

    public Tile getTile(int x, int y) {
        return tiles.get(y).get(x);
    }
}
