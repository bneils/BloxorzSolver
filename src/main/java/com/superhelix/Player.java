package com.superhelix;

public class Player {
    /// The player class represents where the two blocks are, and whether they move together or separately.
    /// If the player is disconnected, the order in which the positions are stored is arbitrary, but the two
    /// coordinates will be sorted by the x-coordinate and then the y-coordinate (if x are the same) before being
    /// put in the identifier.
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

    private void sortPositions() {
        // Sort by x, but also by y to break the tie
        if (first.x() > second.x() || (first.x() == second.x() && first.y() > second.y())) {
            Position temp = first;
            first = second;
            second = temp;
            focus = 1 - focus;
        }
    }

    public boolean calculateIsSplit(Position first, Position second) {
        int dx = Math.abs(first.x() - second.x());
        int dy = Math.abs(first.y() - second.y());
        return !(dx <= 1 && dy == 0 || dx == 0 && dy <= 1);
    }

    public boolean isVertical() {
        return first.x() == second.x() && first.y() == second.y();
    }

    public Player newMovedPlayer(Direction direction, boolean switchFocus) {
        // Switch focus is ignored if isSplit is false
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

            if (first.x() == second.x() && first.y() == second.y()) {
                // if it's vertical each next state is horizontal
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
