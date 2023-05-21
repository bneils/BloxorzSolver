package com.superhelix;

public record Position(int x, int y) {
    public Position addTo(int dx, int dy) {
        return new Position(x + dx, y + dy);
    }
}
