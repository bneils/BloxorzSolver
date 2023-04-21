import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.TreeMap;

public class Main {
    public static void main(String[] args) {
        Level level;
        try {
            level = LevelUtils.loadFromFile("levels/level1.txt");
        } catch (FileNotFoundException | LevelParserException e) {
            System.out.println("error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}