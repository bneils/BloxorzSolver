import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import org.javatuples.Pair;

public class LevelUtils {
    private static boolean isLetter(String s) {
        return s.length() == 1 && Character.isAlphabetic(s.charAt(0));
    }

    public static Level loadFromFile(String filename) throws FileNotFoundException, LevelParserException {
        Level map = new Level();
        File file = new File(filename);
        Scanner reader = new Scanner(file);
        ArrayList<ArrayList<Tile>> level = new ArrayList<>();

        Optional<Pair<Integer, Integer>> playerPos, goalPos;
        playerPos = goalPos = Optional.empty();

        // A tree map is used since the keys are alphanumeric characters.
        // Each key's value contains a list of locations on the level where that element is.
        // This map will hold the locations of every element (switches and bridge tiles) using their alphabetic ID.
        Map<Character, ArrayList<Pair<Integer, Integer>>> elementPositions = new TreeMap<>();
        Map<Character, Boolean> bridgeInitialStatuses = new TreeMap<>();
        Map<Character, SwitchAttribute> switchAttributes = new TreeMap<>();

        for (int lineno = 1; reader.hasNextLine(); ++lineno) {
            String line = reader.nextLine();
            if (line.contains(":")) {
                // process attribute(s)
                String[] parts = line.split(":");
                if (parts.length > 2)
                    throw new LevelParserException("No more than one ':' allowed here on line %d\n"
                            .formatted(lineno));

                String identifier = parts[0].strip();
                char letterId = identifier.charAt(0);

                // Attribute names can only be letters
                if (identifier.length() != 1 || !Character.isAlphabetic(identifier.charAt(0))) {
                    throw new LevelParserException("Invalid attribute name '%s' on line %d"
                            .formatted(identifier, lineno));
                }

                String[] attrs = parts[1].split(",");

                if (Character.isLowerCase(letterId)) {
                    if (attrs.length > 1) {
                        throw new LevelParserException("Too many attributes for bridge '%c' on line %d"
                                .formatted(letterId, lineno));
                    }
                    switch (attrs[0].strip().toLowerCase()) {
                        case "on" -> bridgeInitialStatuses.put(letterId, true);
                        case "off" -> bridgeInitialStatuses.put(letterId, false);
                        default -> throw new LevelParserException("Invalid bridge attribute for '%c' '%s' on line %d"
                                .formatted(letterId, attrs[0], lineno));
                    }
                    continue;
                }
                // Now we can handle switch attributes only
                SwitchAttribute switchAttr = new SwitchAttribute();
                for (String attr : attrs) {
                    String[] tokens = attr
                            .strip()
                            .toLowerCase() // case in-sensitive
                            .split("\\s");
                    Optional<SwitchAction> action = Optional.empty();
                    switch (tokens[0]) {
                        case "soft" -> switchAttr.activationType = ActivationType.SOFT;
                        case "hard" -> switchAttr.activationType = ActivationType.HARD;
                        case "teleport" -> {
                            if (isLetter(tokens[1]) && isLetter(tokens[2]))
                                throw new LevelParserException("Invalid switch teleport destinations for '%c' on line %d"
                                        .formatted(letterId, lineno));
                            switchAttr.teleportTo = Optional.of(new Pair<>(tokens[1].charAt(0), tokens[2].charAt(0)));
                        }
                        case "toggle" -> action = Optional.of(SwitchAction.TOGGLE);
                        case "off" ->  action = Optional.of(SwitchAction.OFF);
                        case "on" -> action = Optional.of(SwitchAction.ON);
                        default -> throw new LevelParserException("Unknown attribute '%s' on line %d"
                                .formatted(tokens[0], lineno));
                    }

                    if (action.isPresent()) {
                        if (tokens.length != 2) {
                            throw new LevelParserException("Attribute '%s' on line %d has wrong number of arguments"
                                    .formatted(tokens[0], lineno));
                        }
                        if (!isLetter(tokens[1])) {
                            throw new LevelParserException("Attribute '%s' on line %d has non-letter noun '%s'"
                                    .formatted(tokens[0], lineno, tokens[1]));
                        }

                        switchAttr.actions.add(new Pair<>(tokens[1].charAt(0), action.get()));
                    }
                }
                switchAttributes.put(letterId, switchAttr);
            } else {
                // build next row of level
                ArrayList<Tile> levelRow = new ArrayList<>();
                for (char c : line.toCharArray()) {
                    Tile tile;
                    int x = levelRow.size();
                    int y = level.size();
                    switch (c) {
                        case ' ' -> tile = Tile.VOID;
                        case '!' -> tile = Tile.WEAK_FLOOR;
                        case '@' -> tile = Tile.STRONG_FLOOR;
                        case '$' -> {
                            tile = Tile.STRONG_FLOOR;
                            if (playerPos.isPresent()) {
                                throw new LevelParserException("Can only have one initial player position on line %d column %d"
                                        .formatted(lineno, x + 1));
                            }
                            playerPos = Optional.of(new Pair<>(x, y));
                        }
                        case '^' -> {
                            tile = Tile.STRONG_FLOOR;
                            if (goalPos.isPresent()) {
                                throw new LevelParserException("Can only have one goal position on line %d column %d"
                                        .formatted(lineno, x + 1));
                            }
                            goalPos = Optional.of(new Pair<>(x, y));
                        }
                        default -> {
                            tile = Tile.STRONG_FLOOR;
                            if (!Character.isAlphabetic(c)) {
                                throw new LevelParserException("Unrecognized character '%c' on line %d column %d"
                                        .formatted(c, lineno, x + 1));
                            }

                            // Ensure c is in the map then add next coordinate
                            ArrayList<Pair<Integer, Integer>> positions = elementPositions
                                    .computeIfAbsent(c, k -> new ArrayList<>());

                            elementPositions.get(c).add(new Pair<>(x, y));

                            // Default bridge condition is ON
                            if (Character.isLowerCase(c)) {
                                bridgeInitialStatuses.computeIfAbsent(c, k -> true);
                            }
                        }
                    }
                    levelRow.add(tile);
                }
                level.add(levelRow);
            }
        }

        if (playerPos.isEmpty())
            throw new LevelParserException("There is no initial player position");
        if (goalPos.isEmpty())
            throw new LevelParserException("There is no goal position");
        for (char c : elementPositions.keySet()) {
            if (Character.isUpperCase(c) && !switchAttributes.containsKey(c))
                throw new LevelParserException("Switch %c does not have any attributes".formatted(c));
        }

        return map;
    }
}
