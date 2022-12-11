package com.trinoxtion.movement.grapple;

import net.punchtree.util.armorstand.ArmorStandUtils;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

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

        // This sets the pitch, but only the yaw matters for rotating the target
        // The armor stand is in the right hand, and pitch rotates the head
        // Yaw rotates the body, which works for the target
        // We're using different items for different target pitches anyway, not armor stand positioning
        gridAlignedLocation.setDirection(facingDirection.getVector());

        ArmorStand armorStand = spawnGrappleTargetArmorStand(gridAlignedLocation);

        GrappleTarget grappleTarget = new GrappleTarget(gridAlignedLocation, facingDirection, armorStand.getUniqueId());
        temporaryGlobalCollection.add(grappleTarget);
    }

    private ArmorStand spawnGrappleTargetArmorStand(Location location) {
        return location.getWorld().spawn(location, ArmorStand.class, stand -> {
            stand.setGravity(false);
            stand.setMarker(true);
            stand.setInvisible(true);
            stand.setInvulnerable(true);
            stand.setCanTick(false);
            ArmorStandUtils.resetPose(stand);
            stand.setItem(EquipmentSlot.HAND, GrappleTarget.TARGET_ITEM);
            stand.addScoreboardTag("loqinttemp");
            stand.addScoreboardTag("grapple-target");
        });
    }

    public Set<GrappleTarget> getTemporaryGlobalCollection() {
        return temporaryGlobalCollection;
    }
}
