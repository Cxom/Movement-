package com.trinoxtion.movement.grapple;

import org.bukkit.util.Vector;

public enum GrappleFacingDirection {

    // NORTH IS negative Z
    // EAST IS positive X
    // SOUTH IS positive Z
    // WEST IS negative X

    STRAIGHT_UP    (0, 1, 0),
    NORTH          (0, 0, -1),
    NORTH_EAST     (1, 0, -1),
    EAST           (1, 0, 0),
    SOUTH_EAST     (1, 0, 1),
    SOUTH          (0, 0, 1),
    SOUTH_WEST     (-1, 0, 1),
    WEST           (-1, 0, 0),
    NORTH_WEST     (-1, 0, -1),
    DOWN_NORTH     (0, -1, -1),
    DOWN_NORTH_EAST(1, -1, -1),
    DOWN_EAST      (1, -1, 0),
    DOWN_SOUTH_EAST(1, -1, 1),
    DOWN_SOUTH     (0, -1, 1),
    DOWN_SOUTH_WEST(-1, -1, 1),
    DOWN_WEST      (-1, -1, 0),
    DOWN_NORTH_WEST(-1, -1, -1),
    UP_NORTH       (0, 1, -1),
    UP_NORTH_EAST  (1, 1, -1),
    UP_EAST        (1, 1, 0),
    UP_SOUTH_EAST  (1, 1, 1),
    UP_SOUTH       (0, 1, 1),
    UP_SOUTH_WEST  (-1,1, 1),
    UP_WEST        (-1,1, 0),
    UP_NORTH_WEST  (-1,1, -1),
    STRAIGHT_DOWN  (0, -1, 0);

    private final Vector vector;

    GrappleFacingDirection(int x, int y, int z) {
        this.vector = new Vector(x, y, z).normalize();
    }

    public Vector getVector() {
        return vector.clone();
    }

    public static GrappleFacingDirection getNearestFacingDirection(Vector direction) {
        double max = -1;
        GrappleFacingDirection bestMatch = NORTH;
        for (GrappleFacingDirection facingDirection : values()) {
            double dot = direction.dot(facingDirection.getVector());
            if (dot > max) {
                max = dot;
                bestMatch = facingDirection;
            }
        }
        return bestMatch;
    }

}
