package com.superhelix;

public enum Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT;

    public int[] toOffset() {
        return switch (this) {
            case UP -> new int[]{0, -1};
            case DOWN -> new int[]{0, 1};
            case LEFT -> new int[]{-1, 0};
            case RIGHT -> new int[]{1, 0};
        };
    }
}
