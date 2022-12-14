package com.trinoxtion.movement.grapple;

import net.punchtree.util.armorstand.ArmorStandUtils;
import org.bukkit.Bukkit;
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
        Bukkit.broadcastMessage("Debug: yaw: " + gridAlignedLocation.getYaw() + " pitch: " + gridAlignedLocation.getPitch());
        gridAlignedLocation.setDirection(facingDirection.getVector());
        if (Math.abs(gridAlignedLocation.getPitch()) == 90) {
            // The yaw doesn't get set by bukkit if the pitch is straight vertical, so the target is askew on first spawn
            // Alternative solution would be just to spawn the model in a standard position and use the grapple target methods to rotate it into place
            // But (and I haven't tested this) it may cause an initial rotate-in-to-place animation to happen
            // TODO decide ^^
            gridAlignedLocation.setYaw(0);
        }

        ArmorStand armorStand = spawnGrappleTargetArmorStand(gridAlignedLocation, facingDirection);

        GrappleTarget grappleTarget = new GrappleTarget(gridAlignedLocation, facingDirection, armorStand.getUniqueId());
        temporaryGlobalCollection.add(grappleTarget);
    }

    private ArmorStand spawnGrappleTargetArmorStand(Location location, GrappleFacingDirection facingDirection) {
        return location.getWorld().spawn(location, ArmorStand.class, stand -> {
            stand.setGravity(false);
            stand.setMarker(true);
            stand.setInvisible(true);
            stand.setInvulnerable(true);
            stand.setCanTick(false);
            ArmorStandUtils.resetPose(stand);
            stand.setItem(EquipmentSlot.HAND, facingDirection.getVerticalComponent().getTargetItem());
            stand.addScoreboardTag("loqinttemp");
            stand.addScoreboardTag("grapple-target");
        });
    }

    public Set<GrappleTarget> getTemporaryGlobalCollection() {
        return temporaryGlobalCollection;
    }

    public void deleteTarget(GrappleTarget grappleTarget) {
        if (!this.temporaryGlobalCollection.remove(grappleTarget)) {
            throw new IllegalArgumentException("Tried to delete a target via the wrong target manager!");
        }
        grappleTarget.getArmorStand().remove();
        // TODO remove from persistence
    }
}
