package com.superhelix;

public record Position(int x, int y) {
    public Position addTo(int dx, int dy) {
        return new Position(x + dx, y + dy);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Position param) {
            return param.x() == x && param.y() == y;
        }
        return false;
    }
}
