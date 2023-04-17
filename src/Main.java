
public class Main {
    public static void main(String[] args) {
        try {
            Level level = LevelUtils.loadFromFile("levels/level1.txt");
        } catch (Exception e) {
            System.out.println("error: " + e.getMessage());
            System.exit(1);
        }
    }
}