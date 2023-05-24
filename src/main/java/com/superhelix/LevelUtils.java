package com.superhelix;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class LevelUtils {
    private static char toLetter(String s) throws LevelParserException {
        if (!(s.length() == 1 && Character.isAlphabetic(s.charAt(0))))
            throw new LevelParserException("String '%s' is not a letter".formatted(s));
        return s.charAt(0);
    }

    private static String[] getTokens(String line, int lineno) throws LevelParserException {
        String[] parts = line.split(":");
        if (parts.length > 2)
            throw new LevelParserException("No more than one ':' allowed here on line %d\n"
                    .formatted(lineno));
        String identifier = parts[0].strip();
        String bridgeId = Character.toString(toLetter(identifier));

        // ID, Attr1, Attr2, ...
        List<String> list = new ArrayList<>(Arrays.asList(parts[1].toLowerCase().split(",")));
        list.replaceAll(String::toLowerCase); // Before we add the ID, the rest should be lowercase
        list.add(0, bridgeId);
        list.replaceAll(String::strip);

        return list.toArray(new String[0]);
    }

    private static boolean getInitialBridgeState(String[] tokens, int lineno) throws LevelParserException {
        // example, a: on
        if (tokens.length != 2) {
            throw new LevelParserException("Wrong number of attributes for bridge '%s' on line %d"
                    .formatted(tokens[0], lineno));
        }
        if (tokens[1].equals("on"))
            return true;
        if (tokens[1].equals("off"))
            return false;

        throw new LevelParserException("Invalid bridge attribute for '%s' ('%s') on line %d"
                .formatted(tokens[0], tokens[1].strip(), lineno));
    }

    private static SwitchAttribute getSwitchAttribute(String[] tokens, int lineno)
            throws LevelParserException {

        ActivationType activationType = ActivationType.SOFT;
        List<BridgeAction> actions = new ArrayList<>();
        char[] teleportLocations = null;

        for (int i = 1; i < tokens.length; ++i) {
            String[] attrWords = tokens[i]
                    .strip()
                    .toLowerCase() // case in-sensitive
                    .split("\\s");

            // verify input first
            switch (attrWords[0]) {
                case "toggle", "off", "on" -> {
                    if (attrWords.length != 2)
                        throw new LevelParserException("Attribute '%s' on line %d has wrong number of arguments"
                                .formatted(attrWords[0], lineno));
                }
                case "teleport" -> {
                    if (attrWords.length != 3) {
                        throw new LevelParserException(
                                "Switch teleport attribute has %d arguments (expected 2) on line %d"
                                        .formatted(attrWords.length, lineno));
                    }
                }
            }

            switch (attrWords[0]) {
                case "soft" -> activationType = ActivationType.SOFT;
                case "hard" -> activationType = ActivationType.HARD;
                case "teleport" -> teleportLocations = new char[] {toLetter(attrWords[1]), toLetter(attrWords[2])};
                case "toggle" -> actions.add(new BridgeAction(toLetter(attrWords[1]), BridgeEffect.TOGGLE));
                case "off" ->    actions.add(new BridgeAction(toLetter(attrWords[1]), BridgeEffect.OFF   ));
                case "on" ->     actions.add(new BridgeAction(toLetter(attrWords[1]), BridgeEffect.ON    ));
                default -> throw new LevelParserException("Unknown attribute '%s' on line %d"
                        .formatted(attrWords[0], lineno));
            }
        }

        return new SwitchAttribute(activationType, actions, teleportLocations);
    }

    public static Level loadFromFile(String filename) throws FileNotFoundException, LevelParserException {
        File file = new File(filename);
        Scanner reader = new Scanner(file);

        Map<Character, TileMetadata> tilesMetadata = new TreeMap<>();
        List<List<Tile>> tiles = new ArrayList<>();
        Position playerPos = null, goalPos = null;

        for (int lineno = 1; reader.hasNextLine(); ++lineno) {
            String line = reader.nextLine();
            if (line.contains(":")) {
                String[] tokens = getTokens(line, lineno);
                char letterId = toLetter(tokens[0]);

                if (!tilesMetadata.containsKey(letterId))
                    tilesMetadata.put(letterId, new TileMetadata(letterId));
                TileMetadata meta = tilesMetadata.get(letterId);

                if (Character.isLowerCase(letterId)) // Bridge
                    meta.setStartingBridgeState(getInitialBridgeState(tokens, lineno));
                else // Switch
                    meta.setSwitchAttribute(getSwitchAttribute(tokens, lineno));
            } else {
                // build next row of level
                List<Tile> levelRow = new ArrayList<>();
                for (char c : line.toCharArray()) {
                    Tile tile;
                    int x = levelRow.size();
                    int y = tiles.size();
                    switch (c) {
                        case ' ' -> tile = Tile.VOID;
                        case '!' -> tile = Tile.WEAK_FLOOR;
                        case '@' -> tile = Tile.STRONG_FLOOR;
                        case '$' -> {
                            tile = Tile.STRONG_FLOOR;
                            if (playerPos != null) {
                                throw new LevelParserException("Can only have one initial player position on line %d column %d"
                                        .formatted(lineno, x + 1));
                            }
                            playerPos = new Position(x, y);
                        }
                        case '^' -> {
                            tile = Tile.STRONG_FLOOR;
                            if (goalPos != null) {
                                throw new LevelParserException("Can only have one goal position on line %d column %d"
                                        .formatted(lineno, x + 1));
                            }
                            goalPos = new Position(x, y);
                        }
                        default -> {
                            tile = Tile.STRONG_FLOOR;
                            if (!Character.isAlphabetic(c)) {
                                throw new LevelParserException("Unrecognized character '%c' on line %d column %d"
                                        .formatted(c, lineno, x + 1));
                            }

                            // Ensure c is in the map then add next coordinate
                            if (!tilesMetadata.containsKey(c))
                                tilesMetadata.put(c, new TileMetadata(c));
                            TileMetadata meta = tilesMetadata.get(c);
                            if (meta.getPositions() == null)
                                meta.setPositions(new ArrayList<>());

                            meta.getPositions().add(new Position(x, y));
                        }
                    }
                    levelRow.add(tile);
                }
                tiles.add(levelRow);
            }
        }

        if (playerPos == null)
            throw new LevelParserException("There is no initial player position");
        if (goalPos == null)
            throw new LevelParserException("There is no goal position");
        for (Map.Entry<Character, TileMetadata> entry : tilesMetadata.entrySet())
            if (Character.isUpperCase(entry.getKey()) && entry.getValue().getSwitchAttribute() == null)
                throw new LevelParserException("Switch '%c' does not have any attributes".formatted(entry.getKey()));

        // Verify that teleport locations are unambiguous
        for (Map.Entry<Character, TileMetadata> entry : tilesMetadata.entrySet()) {
            SwitchAttribute attr = entry.getValue().getSwitchAttribute();
            // We don't know for sure that this metadata is for a switch
            // We could check the case for the character, but this is clearer
            if (attr == null)
                break;
            char[] teleportLocations = attr.teleportLocations();
            if (teleportLocations != null) {
                for (char ch : teleportLocations) {
                    if (!tilesMetadata.containsKey(ch)) {
                        throw new LevelParserException("Switch '%c' has unknown teleport location '%c'"
                                .formatted(entry.getKey(), ch));
                    }
                    if (tilesMetadata.get(ch).getPositions().size() != 1) {
                        throw new LevelParserException("Switch '%c' has ambiguous teleport to tile '%c'"
                                .formatted(entry.getKey(), ch));
                    }
                }
            }
        }

        return new Level(
                tiles, playerPos, goalPos, tilesMetadata
        );
    }
}
