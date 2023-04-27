package com.superhelix;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.javatuples.Pair;

// Contains the original level
public class Level {
    public ArrayList<ArrayList<Tile>> cells = new ArrayList<>();
    public Pair<Integer, Integer> playerPos = null, goalPos = null;

    // A tree map is used since the keys are alphanumeric characters.
    // Each key's value contains a list of locations on the level where that element is.
    // This map will hold the locations of every element (switches and bridge tiles) using their alphabetic ID.
    public Map<Character, SwitchAttribute> switchAttributes = new TreeMap<>();
    public Map<Character, Boolean> bridgeInitialStatuses = new TreeMap<>();
    public Map<Character, ArrayList<Pair<Integer, Integer>>> elementPositions = new TreeMap<>();

    public ArrayList<ArrayList<Tile>> applyState(Map<Character, Boolean> states) {
        // Do a weird clone of cells
        ArrayList<ArrayList<Tile>> newCells = new ArrayList<>();
        for (ArrayList<Tile> row : cells) {
            ArrayList<Tile> newRow = new ArrayList<Tile>(row.size());
            newRow.addAll(row);
            newCells.add(newRow);
        }

        // Apply each state to bridge locations
        for (Map.Entry<Character, Boolean> entry : states.entrySet()) {
            Tile newTile = entry.getValue() ? Tile.STRONG_FLOOR : Tile.VOID;
            for (Pair<Integer, Integer> pos : elementPositions.get(entry.getKey())) {
                int x = pos.getValue0();
                int y = pos.getValue1();
                newCells.get(y).set(x, newTile);
            }
        }

        return newCells;
    }
}
