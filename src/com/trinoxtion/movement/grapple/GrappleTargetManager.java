package com.trinoxtion.movement.grapple;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class GrappleTargetManager {

    private Set<GrappleTarget> temporaryGlobalCollection = new HashSet<>();

    public void summonGrappleTarget(Player player) {
        Location gridAlignedLocation = player.getEyeLocation();
        gridAlignedLocation.setX( Math.round(gridAlignedLocation.getX() * 2) / 2.0);
        gridAlignedLocation.setY( Math.round(gridAlignedLocation.getY() * 2) / 2.0);
        gridAlignedLocation.setZ( Math.round(gridAlignedLocation.getZ() * 2) / 2.0);

        GrappleFacingDirection facingDirection = GrappleFacingDirection.getNearestFacingDirection(player.getLocation().getDirection());
        GrappleTarget grappleTarget = new GrappleTarget(gridAlignedLocation, facingDirection);

        temporaryGlobalCollection.add(grappleTarget);
    }
}
