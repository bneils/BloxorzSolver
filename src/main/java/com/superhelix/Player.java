package com.superhelix;

/// The player class represents where the two blocks are, and whether they move together or separately.
/// The two coordinates will be sorted by the x-coordinate and then the y-coordinate (if x are the same), but won't
/// change where the focus is.
public class Player {
    private Position first, second;
    private final boolean isSplit;
    private int focus;

    public Player(Position first, Position second) {
        this.first = first;
        this.second = second;
        isSplit = calculateIsSplit(first, second);
        focus = 0;
        sortPositions();
    }

    public Player(Position first, Position second, int focus) {
        this.first = first;
        this.second = second;
        isSplit = calculateIsSplit(first, second);
        if (isSplit)
            this.focus = focus;
        sortPositions();
    }

    /**
     * Internally sorts the positions by x and then by y.
     * Note that while this function may appear to toggle the focus, that focus is still on the same position.
     */
    private void sortPositions() {
        // Sort by x, but also by y to break the tie
        if (first.x() > second.x() || (first.x() == second.x() && first.y() > second.y())) {
            Position temp = first;
            first = second;
            second = temp;
            focus = 1 - focus;
        }
    }

    /**
     * Determines if the player was split apart (by a teleport switch)
     * @param first The first position
     * @param second The second position
     * @return Whether the player is split in-half, or it doesn't resemble a normal horizontal or vertical slab.
     */
    private static boolean calculateIsSplit(Position first, Position second) {
        int dx = Math.abs(first.x() - second.x());
        int dy = Math.abs(first.y() - second.y());
        return !(dx <= 1 && dy == 0 || dx == 0 && dy <= 1);
    }

    public boolean isVertical() {
        return first.x() == second.x() && first.y() == second.y();
    }

    /**
     * Creates a copy of the player but moved in a direction, preserving focus
     * @param direction The direction to move in
     * @param switchFocus Whether to switch focus before moving
     * @return The moved player
     */
    public Player newMovedPlayer(Direction direction, boolean switchFocus) {
        Position newFirst, newSecond;

        int[] offset = direction.toOffset();
        int dx = offset[0];
        int dy = offset[1];

        int newFocus = (switchFocus) ? 1 - focus : focus;

        if (isSplit) {
            if (newFocus == 0) {
                newFirst = first.addTo(dx, dy);
                newSecond = second;
            } else {
                newFirst = first;
                newSecond = second.addTo(dx, dy);
            }
        } else {
            int x1 = first.x();
            int y1 = first.y();
            int x2 = second.x();
            int y2 = second.y();

            // vertical -> horizontal
            // whereas, horizontal may turn into either
            if (first.equals(second)) {
                if (dx != 0) {
                    x1 = (dx == 1) ? first.x() + 1 : first.x() - 2;
                    x2 = x1 + 1;
                } else {
                    y1 = (dy == 1) ? first.y() + 1 : first.y() - 2;
                    y2 = y1 + 1;
                }
            } else {
                if (dx != 0) { // left, right
                    if (first.x() != second.x()) {
                        // <- ## ->
                        x2 = x1 = ((dx > 0) ? second.x() : first.x()) + dx;
                    } else {
                        // <- # ->
                        // <- # ->
                        x2 = x1 = first.x() + dx;
                    }
                } else if (dy != 0) { // up, down
                    if (first.x() != second.x()) {
                        // ^^
                        // ##
                        // vv
                        y2 = y1 = first.y() + dy;
                    } else {
                        // ^
                        // #
                        // #
                        // v
                        y1 = y2 = ((dy > 0) ? second.y() : first.y()) + dy;
                    }
                }
            }
            newFirst = new Position(x1, y1);
            newSecond = new Position(x2, y2);
        }
        // newFocus will be ignored if the player isn't split
        return new Player(newFirst, newSecond, newFocus);
    }

    public String formatIdentifier() {
        return "(%d,%d),(%d,%d),".formatted(first.x(), first.y(), second.x(), second.y());
    }

    public Position getFirst() { return first; }
    public Position getSecond() { return second; }
    public boolean isSplit() { return isSplit; }
}
