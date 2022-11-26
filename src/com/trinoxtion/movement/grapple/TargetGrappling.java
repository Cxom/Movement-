package com.trinoxtion.movement.grapple;

import com.trinoxtion.movement.MovementPlayer;
import com.trinoxtion.movement.MovementPlusPlus;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TargetGrappling implements Listener {

    private static final int ARROW_TRACKING_TIMEOUT_TICKS = 200;
    private static final Vector TEST_TARGET_FACING_DIRECTION = new Vector(-1, 0, 0);
    private static final ItemStack TEST_TARGET_ITEM = new ItemStack(Material.LEATHER_CHESTPLATE);
    static {
        TEST_TARGET_ITEM.editMeta(meta -> meta.setCustomModelData(200));
    }
    private static final int TARGET_RADIUS = 1;

    private final Set<GrappleTarget> grappleTargets;
    private final Set<TrackedArrow> trackedArrows = new HashSet<>();

    private final BukkitTask projectileCalculationTask;

    public TargetGrappling(Set<GrappleTarget> grappleTargets) {
        this.grappleTargets = grappleTargets;

        projectileCalculationTask = Bukkit.getScheduler().runTaskTimer(MovementPlusPlus.getPlugin(), () -> {
            Iterator<TrackedArrow> arrowIterator = trackedArrows.iterator();
            arrowLoop: while(arrowIterator.hasNext()) {
                TrackedArrow trackedArrow = arrowIterator.next();
                Arrow arrow = trackedArrow.arrow;
                Location arrowCurrentLocation = arrow.getLocation();



//                trackedArrow.shooter.sendMessage(trackedArrow.potentialTargets.size() + " potential target" + (trackedArrow.potentialTargets.size() == 1 ? "" : "s"));

                Iterator<GrappleTarget> targetIterator = trackedArrow.potentialTargets.iterator();
                while (targetIterator.hasNext()) {
                    GrappleTarget target = targetIterator.next();

//                    trackedArrow.shooter.sendMessage("Checking target " + String.format("%.03f %.03f %.03f", target.location().getX(), target.location().getY(), target.location().getZ()));

                    Vector lastDifference = target.location().toVector().subtract(trackedArrow.lastLocation.toVector());
                    Vector currentDifference = target.location().toVector().subtract(arrowCurrentLocation.toVector());

                    if (arrowCrossedTargetPlane(target, lastDifference, currentDifference)) {

                        Location intersection = calculateHitLocation(target, trackedArrow, arrowCurrentLocation);
                        double intersectionDistance = intersection.distance(target.location());
                        double playerDistance = intersection.distance(trackedArrow.shooter.getLocation());

                        if (intersectionDistance < TARGET_RADIUS) {
                            doTargetHitPolish(target, trackedArrow, arrow, intersection, playerDistance);
                            arrow.remove();
                            // TODO store this in the shooter?

                            target.startGrapple(MovementPlusPlus.getMovementPlayer(trackedArrow.shooter));

                            arrowIterator.remove();
                            continue arrowLoop; // This is a huge code smell, don't forget to refactor it once done
                        } else {
//                            trackedArrow.shooter.sendMessage("Plane hit detected (distance " + String.format("%.03f", intersectionDistance) + " - removing target iterator)");
                            targetIterator.remove();
//                            trackedArrow.shooter.sendMessage("target iterator size now " + trackedArrow.potentialTargets.size());
                        }
                    }
                }

                // This has to be done after checking target intersection, otherwise arrows stuck in the block behind a target will not register as a hit before being removed from tracking
                if (trackedArrow.potentialTargets.isEmpty() || arrow.isInBlock() || arrowCurrentLocation.getY() < -128 || arrow.getTicksLived() > ARROW_TRACKING_TIMEOUT_TICKS || !arrow.isTicking()) {
//                    arrow.remove();
//                    trackedArrow.shooter.sendMessage("Arrow removed");
                    arrowIterator.remove();
                    continue;
                }

                trackedArrow.updateLocation();
            }
        }, 0, 1);
    }

    public void cancelTask() {
        if (projectileCalculationTask != null) {
            projectileCalculationTask.cancel();
        }
    }

    private boolean arrowCrossedTargetPlane(GrappleTarget target, Vector lastDifference, Vector currentDifference) {
        return lastDifference.dot(target.facingDirection()) < 0 && currentDifference.dot(target.facingDirection()) >= 0;
    }

    @NotNull
    private Location calculateHitLocation(GrappleTarget target, TrackedArrow trackedArrow, Location currentLocation) {
        // Calculate plane intersection
        Vector arrowDirection = currentLocation.toVector().subtract(trackedArrow.lastLocation.toVector());
        Vector arrowToTarget = target.location().toVector().subtract(trackedArrow.lastLocation.toVector());
        double t = arrowToTarget.dot(target.facingDirection()) / arrowDirection.dot(target.facingDirection());
        Location intersection = trackedArrow.lastLocation.clone().add(arrowDirection.multiply(t));
        return intersection;
    }

    private void doTargetHitPolish(GrappleTarget target, TrackedArrow trackedArrow, Arrow arrow, Location intersection, double playerDistance) {
        trackedArrow.shooter.sendMessage("Target hit detected");
        trackedArrow.shooter.playSound(trackedArrow.shooter.getLocation(), Sound.ITEM_TRIDENT_HIT_GROUND, (float) (5. / Math.max(playerDistance / 5, 1)), 1);
        trackedArrow.shooter.playSound(trackedArrow.shooter.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 15, 1);
        target.location().getWorld().playSound(intersection, Sound.BLOCK_WOOL_HIT, 2, 1);
        target.location().getWorld().spawnParticle(Particle.BLOCK_DUST, intersection, 50, Bukkit.createBlockData(Material.BONE_BLOCK));
    }

    @EventHandler
    public void onPlayerShoot(EntityShootBowEvent event) {
        if ( ! (event.getEntity() instanceof Player player)) { return; }
        if ( ! MovementPlusPlus.isMovementPlayer(player)) { return; }

        Vector direction = event.getProjectile().getVelocity();

        // TODO ZERO OUT THE Y COMPONENT _ DON'T CARE ABOUT GRAVITY
        if (direction.dot(TEST_TARGET_FACING_DIRECTION) >= 0) {
            return;
        }
        // arrow velocity is going toward the target
        // start tracking arrow for intersection;
        if (event.getProjectile() instanceof Arrow arrow) {
            trackedArrows.add(new TrackedArrow(arrow, new HashSet<>(grappleTargets)));
        }
    }

    @EventHandler
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        if (event.isSneaking()) {
            MovementPlayer movementPlayer = MovementPlusPlus.getMovementPlayer(event.getPlayer());
            if (movementPlayer != null && movementPlayer.isGrappling()) {
                movementPlayer.stopGrapple();
            }
        }
    }

    static class TrackedArrow {
        final Arrow arrow;
        final Player shooter;
        final MovementPlayer movementPlayer;
        Location lastLocation;
        final Set<GrappleTarget> potentialTargets;

        public TrackedArrow(Arrow arrow, Set<GrappleTarget> potentialTargets) {
            this.arrow = arrow;
            this.shooter = (Player) arrow.getShooter();
            assert shooter != null;
            this.movementPlayer = MovementPlusPlus.getMovementPlayer(shooter);
            this.lastLocation = arrow.getLocation();
            this.potentialTargets = potentialTargets;
        }

        public void updateLocation() {
            this.lastLocation = arrow.getLocation();
        }
    }

}