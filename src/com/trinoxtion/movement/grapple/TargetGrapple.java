package com.trinoxtion.movement.grapple;

import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TargetGrapple implements Listener {

    // NOTE this might cause an error depending on how this plugin is loaded (if it's loaded before the Quarantine world is - not sure if this is caused by load property of plugin.yml)
    // facing negative x (x axis = 1141)
    private static Location TEST_TARGET;
    private static final ItemStack TEST_TARGET_ITEM = new ItemStack(Material.LEATHER_CHESTPLATE);
    static {
        TEST_TARGET_ITEM.editMeta(meta -> meta.setCustomModelData(200));
    }
    private static final Vector TEST_TARGET_FACING_DIRECTION = new Vector(-1, 0, 0);
    public static final int TARGET_RADIUS = 1;
    public void cancelTask() {
        if (projectileCalculationTask != null) {
            projectileCalculationTask.cancel();
        }
    }

    static class TrackedArrow {

        final Arrow arrow;
        final Player shooter;
        Location lastLocation;

        public TrackedArrow(Arrow arrow) {
            this.arrow = arrow;
            this.shooter = (Player) arrow.getShooter();
            this.lastLocation = arrow.getLocation();
        }

        public void update() {
            this.lastLocation = arrow.getLocation();
        }
    }

    Set<TrackedArrow> trackedArrows = new HashSet<>();

    BukkitTask projectileCalculationTask;

    public TargetGrapple() {
        TEST_TARGET = new Location(Bukkit.getWorld("Quarantine"), 1141, 76, -2971, 90, 0);
        projectileCalculationTask = Bukkit.getScheduler().runTaskTimer(Bukkit.getPluginManager().getPlugin("MovementPlusPlus"), () -> {
            Iterator<TrackedArrow> iterator = trackedArrows.iterator();
            while(iterator.hasNext()) {
                TrackedArrow trackedArrow = iterator.next();
                Arrow arrow = trackedArrow.arrow;
                Location lastLocation = trackedArrow.lastLocation;
                Location currentLocation = arrow.getLocation();
                Vector lastDifference = TEST_TARGET.toVector().subtract(lastLocation.toVector());
                Vector currentDifference = TEST_TARGET.toVector().subtract(currentLocation.toVector());
                if (lastDifference.dot(TEST_TARGET_FACING_DIRECTION) < 0 && currentDifference.dot(TEST_TARGET_FACING_DIRECTION) >= 0) {

                    // Calculate plane intersection
                    Vector arrowDirection = currentLocation.toVector().subtract(lastLocation.toVector());
                    Vector arrowToTarget = TEST_TARGET.toVector().subtract(lastLocation.toVector());
                    double t = arrowToTarget.dot(TEST_TARGET_FACING_DIRECTION) / arrowDirection.dot(TEST_TARGET_FACING_DIRECTION);
                    // NOTE MUTATING LAST LOCATION FOR EFFICIENCY - NOT VALID AFTER THIS POINT
                    Location intersection = lastLocation.add(arrowDirection.multiply(t));

                    double intersectionDistance = intersection.distance(TEST_TARGET);

                    double playerDistance = intersection.distance(trackedArrow.shooter.getLocation());

                    if (intersectionDistance < TARGET_RADIUS) {
                        trackedArrow.shooter.sendMessage("Target hit detected (shot by " + ((Player) arrow.getShooter()).getName() + ")");
                        trackedArrow.shooter.playSound(trackedArrow.shooter.getLocation(), Sound.ITEM_TRIDENT_HIT_GROUND, (float) (5. / Math.max(playerDistance / 5, 1)), 1);
                        TEST_TARGET.getWorld().playSound(intersection, Sound.BLOCK_WOOL_HIT, 2, 1);
                        TEST_TARGET.getWorld().spawnParticle(Particle.BLOCK_DUST, intersection, 50, Bukkit.createBlockData(Material.BONE_BLOCK));
//                        TEST_TARGET.getWorld().spawnParticle(Particle.ITEM_CRACK, TEST_TARGET, 100, TEST_TARGET_ITEM);
//                        TEST_TARGET.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, TEST_TARGET, 100);
                        arrow.remove();
                    } else {
                        trackedArrow.shooter.sendMessage("Plane hit detected (not target - dist " + String.format("%.3f", intersectionDistance) + ", shot by " + trackedArrow.shooter.getName() + ")");
                    }

                    iterator.remove();
                } else if (arrow.getTicksLived() > 200 || !arrow.isValid()) {
                    arrow.remove();
                    iterator.remove();
                    trackedArrow.shooter.sendMessage("Removed tracked arrow");
                } else if (arrow.isInBlock()) {
                    iterator.remove();
                    trackedArrow.shooter.sendMessage("Removed landed arrow");
                }

                trackedArrow.update();
            }
        }, 0, 1);
    }

    @EventHandler
    public void onPlayerShoot(EntityShootBowEvent event) {
        if ( ! (event.getEntity() instanceof Player player)) { return; }

        Vector direction = event.getProjectile().getVelocity();

        // TODO ZERO OUT THE Y COMPONENT _ DON'T CARE ABOUT GRAVITY
        if (direction.dot(TEST_TARGET_FACING_DIRECTION) >= 0) {
            return;
        }
        // arrow velocity is going toward the target
        // start tracking arrow for intersection;
        if (event.getProjectile() instanceof Arrow arrow) {
            trackedArrows.add(new TrackedArrow(arrow));
        }
    }





}
