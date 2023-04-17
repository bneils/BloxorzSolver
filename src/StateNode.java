
public class StateNode implements Cloneable {
    private int distance;   // The number of moves traveled from start to be here
    private int x1, y1, x2, y2;     // The player's position
    private boolean isSplit;
    private boolean[] buttonStates;

    public StateNode(int distance, int x1, int y1, int x2, int y2, boolean isSplit, boolean[] buttonStates) {
        this.distance = distance;
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.isSplit = isSplit;
        this.buttonStates = buttonStates;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }


}
