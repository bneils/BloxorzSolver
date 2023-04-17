import java.util.ArrayList;
import java.util.Optional;
import org.javatuples.Pair;

public class SwitchAttribute {
    public int x, y;
    public ActivationType activationType = ActivationType.SOFT;
    public ArrayList<Pair<Character, SwitchAction>> actions = new ArrayList<>();

    public Optional<Pair<Character, Character>> teleportTo = Optional.empty();
}