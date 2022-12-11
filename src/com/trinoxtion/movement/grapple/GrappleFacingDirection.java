package com.trinoxtion.movement.grapple;

import org.bukkit.inventory.ItemStack;
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

    /**
     * This actually returns the facing direction OPPOSITE direction so that the target is facing towards the vector origin
     */
    public static GrappleFacingDirection getNearestFacingDirection(Vector direction) {
        direction = direction.clone().multiply(-1);
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

    VerticalComponent getVerticalComponent() {
        if (this == STRAIGHT_DOWN) {
            return VerticalComponent.STRAIGHT_DOWN;
        } else if (this == STRAIGHT_UP) {
            return VerticalComponent.STRAIGHT_UP;
        } else if (this.name().startsWith("DOWN")) {
            return VerticalComponent.DOWN_45;
        } else if (this.name().startsWith("UP")) {
            return VerticalComponent.UP_45;
        } else {
            return VerticalComponent.LEVEL;
        }
    }

    enum VerticalComponent {
        STRAIGHT_UP(GrappleTarget.TARGET_ITEM_STRAIGHT_UP),
        UP_45(GrappleTarget.TARGET_ITEM_UP_45),
        LEVEL(GrappleTarget.TARGET_ITEM),
        DOWN_45(GrappleTarget.TARGET_ITEM_DOWN_45),
        STRAIGHT_DOWN(GrappleTarget.TARGET_ITEM_STRAIGHT_DOWN);

        private final ItemStack targetItem;
        VerticalComponent(ItemStack targetItem) {
            this.targetItem = targetItem;
        }

        ItemStack getTargetItem() {
            return targetItem;
        }
    }

}
