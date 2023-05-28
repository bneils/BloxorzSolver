package com.superhelix;

import java.util.*;

public record Level(
        List<List<Tile>> tiles,
        Map<Character, TileMetadata> tilesMetadata
) {
    /**
     * Applies a map of tile states to the level, leaving some tiles to be no longer physical if they're powered off
     * @param states The map of states from switches and bridges to their state
     * @return A matrix of tiles
     */
    public List<List<Tile>> applyState(Map<Character, Boolean> states) {
        List<List<Tile>> newTiles = new ArrayList<>();
        for (List<Tile> row : tiles) {
            List<Tile> newRow = new ArrayList<>(row.size());
            newRow.addAll(row);
            newTiles.add(newRow);
        }

        for (Map.Entry<Character, Boolean> entry : states.entrySet()) {
            Tile newTile = entry.getValue() ? Tile.STRONG_FLOOR : Tile.VOID;
            for (Position pos : tilesMetadata.get(entry.getKey()).getPositions())
                newTiles.get(pos.y()).set(pos.x(), newTile);
        }

        return newTiles;
    }
}
