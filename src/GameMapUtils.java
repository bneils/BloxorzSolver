import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class GameMapUtils {
    public static GameMap loadFromFile(String filename) throws FileNotFoundException, LevelParserException {
        GameMap map = new GameMap();
        File file = new File(filename);
        Scanner reader = new Scanner(file);

        for (int lineno = 1; reader.hasNextLine(); ++lineno) {
            String line = reader.nextLine();
            if (line.contains(":")) {
                // process attribute(s)
                String[] parts = line.split(":");
                if (parts.length > 2)
                    throw new LevelParserException("error: no more than one ':' allowed here (line %d)\n".formatted(lineno));

                String identifier = parts[0];
                String[] attrs = parts[1].split(",");
                for (String attr : attrs) {

                }
            } else {
                // build next row of map
                if (line.contains("\t"))
                    throw new LevelParserException("error: tabs are not allowed (line %d)\n".formatted(lineno));
            }
        }

        return map;
    }
}
