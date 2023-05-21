package com.superhelix;

import java.util.*;

import org.javatuples.Pair;

// Contains the original level
public record Level(
        List<List<Tile>> tiles,
        Position playerPos,
        Position goalPos,
        Map<Character, TileMetadata> tilesMetadata
) {
    public List<List<Tile>> applyState(Map<Character, Boolean> bridgeStates) {
        // Do a clone of tiles
        List<List<Tile>> newTiles = new ArrayList<>();
        for (List<Tile> row : tiles) {
            List<Tile> newRow = new ArrayList<>(row.size());
            newRow.addAll(row);
            newTiles.add(newRow);
        }

        // Update the (jagged) tile grid using each bridge state, because we want to use the changes that happened to
        // dictate where the player can move
        for (Map.Entry<Character, Boolean> entry : bridgeStates.entrySet()) {
            Tile newTile = entry.getValue() ? Tile.STRONG_FLOOR : Tile.VOID;
            for (Position pos : tilesMetadata.get(entry.getKey()).getPositions())
                newTiles.get(pos.y()).set(pos.x(), newTile);
        }

        return newTiles;
    }

    public Tile getTile(int x, int y) {
        return tiles.get(y).get(x);
    }
}
