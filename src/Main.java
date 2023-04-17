import java.io.FileNotFoundException;

public class Main {
    public static void main(String[] args) {
        try {
            Level level = LevelUtils.loadFromFile("levels/level0.txt");
        } catch (Exception e) {
            System.out.println("error: " + e.getMessage());
        }

        System.out.println("Loaded level!");
    }
}