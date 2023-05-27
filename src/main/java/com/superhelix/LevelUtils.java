package com.superhelix;

import java.io.*;
import java.util.*;

public class LevelUtils {
    private static char toTileCharacter(String s) throws LevelParserException {
        if (s.length() > 1)
            throw new LevelParserException("Name '%s' is too long".formatted(s));
        char c = s.charAt(0);
        if (!(Character.isAlphabetic(c) || c == '$' || c == '^'))
            throw new LevelParserException("Name '%c' is not valid".formatted(c));
        return c;
    }

    public static Level loadFromFile(String levelFilename, String infoFilename) throws FileNotFoundException, LevelParserException {
        Scanner levelScanner = new Scanner(new File(levelFilename));

        Map<Character, TileMetadata> tilesMetadata = new TreeMap<>();
        List<List<Tile>> tiles = new ArrayList<>();
        int lineno;

        // Builds tile matrix and records their locations
        lineno = 1;
        while (levelScanner.hasNextLine()) {
            String line = levelScanner.nextLine();
            int y = lineno - 1;
            Tile tile;
            List<Tile> tilesRow = new ArrayList<>();
            for (int x = 0; x < line.length(); ++x) {
                char c = line.charAt(x);
                switch (c) {
                    case ' ' -> tile = Tile.VOID;
                    case '!' -> tile = Tile.WEAK_FLOOR;
                    case '@' -> tile = Tile.STRONG_FLOOR;
                    case '$', '^' -> {
                        tile = Tile.STRONG_FLOOR;
                        if (tilesMetadata.containsKey(c))
                            throw new LevelParserException("Can only have one of '%c' on %d:%d"
                                    .formatted(c, lineno, x + 1));
                        tilesMetadata.put(c, new TileMetadata(
                                c, null, true, new ArrayList<>(
                                Collections.singleton(new Position(x, y)))
                        ));
                    }
                    default -> {
                        tile = Tile.STRONG_FLOOR;
                        if (!Character.isAlphabetic(c))
                            throw new LevelParserException("Identifier character '%c' must be alphabetic on %d:%d"
                                    .formatted(c, lineno, x + 1));
                        if (!tilesMetadata.containsKey(c))
                            tilesMetadata.put(c, new TileMetadata(c));
                        TileMetadata meta = tilesMetadata.get(c);
                        if (meta.getPositions() == null)
                            meta.setPositions(new ArrayList<>());

                        meta.getPositions().add(new Position(x, y));
                    }
                }
                tilesRow.add(tile);
            }
            tiles.add(tilesRow);
            ++lineno;
        }

        if (infoFilename != null && infoFilename.length() > 0) {
            Scanner infoScanner = new Scanner(new File(infoFilename));
            lineno = 1;
            while (infoScanner.hasNextLine()) {
                String line = infoScanner.nextLine();
                String[] halves = line.split(":");
                if (halves.length != 2)
                    throw new LevelParserException("One colon must be used on line %d".formatted(lineno));
                String name = halves[0];
                if (name.length() > 1)
                    throw new LevelParserException("Name before colon must be 1 character on line %d".formatted(lineno));
                char c = name.charAt(0);
                TileMetadata tileMetadata = tilesMetadata.get(c);
                if (tileMetadata == null)
                    throw new LevelParserException("Unknown tile character '%c' on line %d".formatted(c, lineno));

                ActivationType activationType = ActivationType.SOFT;
                List<TileAction> bridgeActions = new ArrayList<>();
                Position[] teleportLocations = null;

                for (String arg : halves[1].split(",")) {
                    String singleAttr = arg.strip();
                    String[] tokens = singleAttr.split("\\s+");
                    if (tokens.length == 0)
                        continue;
                    switch (tokens[0]) {
                        case "on", "off" -> {
                            boolean bridgeState = tokens[0].equals("on");
                            if (tokens.length == 1) {
                                tileMetadata.setStartingBridgeState(bridgeState);
                            } else if (tokens.length == 2) {
                                if (tokens[1].length() != 1)
                                    throw new LevelParserException(
                                            "%s parameter must be 1 character on line %d".formatted(tokens[0], lineno));
                                char bridgeId = tokens[1].charAt(0);
                                if (!tilesMetadata.containsKey(bridgeId))
                                    throw new LevelParserException(
                                            "%s parameter does not exist in tile matrix on line %d".formatted(tokens[0], lineno));
                                bridgeActions.add(
                                        new TileAction(bridgeId, bridgeState ? BridgeEffect.ON : BridgeEffect.OFF));
                            } else {
                                throw new LevelParserException(
                                        "invalid number of parameters for '%s' on line %d".formatted(tokens[0], lineno));
                            }
                        }
                        case "toggle" -> {
                            if (tokens.length != 2)
                                throw new LevelParserException(
                                        "expecting one parameter for toggle on line %d".formatted(lineno));
                            if (tokens[1].length() != 1)
                                throw new LevelParserException(
                                        "%s parameter must be 1 character on line %d".formatted(tokens[0], lineno));
                            char bridgeId = tokens[1].charAt(0);
                            if (!tilesMetadata.containsKey(bridgeId))
                                throw new LevelParserException(
                                        "%s parameter does not exist in tile matrix on line %d".formatted(tokens[0], lineno));
                            bridgeActions.add(new TileAction(bridgeId, BridgeEffect.TOGGLE));
                        }
                        case "teleport" -> {
                            if (tokens.length != 3)
                                throw new LevelParserException(
                                        "expecting two parameters for teleport on line %d".formatted(lineno));
                            if (tokens[1].length() != 1 || tokens[2].length() != 1)
                                throw new LevelParserException(
                                        "teleport parameter(s) must be 1 character on line %d".formatted(lineno));
                            char firstBridgeId = tokens[1].charAt(0);
                            char secondBridgeId = tokens[2].charAt(0);
                            if (!tilesMetadata.containsKey(firstBridgeId) || !tilesMetadata.containsKey(secondBridgeId))
                                throw new LevelParserException(
                                        "%c or %c bridge does not exist in tile matrix on line %d".formatted(
                                                firstBridgeId, secondBridgeId, lineno));
                            List<Position> positions1 = tilesMetadata.get(firstBridgeId).getPositions();
                            List<Position> positions2 = tilesMetadata.get(secondBridgeId).getPositions();
                            if (positions1.size() > 1 || positions2.size() > 1)
                                throw new LevelParserException("A teleport argument has more than 1 position on line %d".formatted(lineno));
                            teleportLocations = new Position[]{positions1.get(0), positions2.get(0)};
                        }
                        case "soft" -> activationType = ActivationType.SOFT;
                        case "hard" -> activationType = ActivationType.HARD;
                        default ->
                                throw new LevelParserException("unknown attribute '%s' on line %d".formatted(tokens[0], lineno));
                    }
                }

                tileMetadata.setSwitchAttribute(new SwitchAttribute(activationType, bridgeActions, teleportLocations));
                ++lineno;
            }
        }

        if (!tilesMetadata.containsKey('$'))
            throw new LevelParserException("There is no initial player position");
        if (!tilesMetadata.containsKey('^'))
            throw new LevelParserException("There is no goal position");

        return new Level(
                tiles, tilesMetadata
        );
    }
}
