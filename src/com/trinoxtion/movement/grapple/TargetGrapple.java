package com.trinoxtion.movement.grapple;

import com.destroystokyo.paper.ParticleBuilder;
import com.trinoxtion.movement.MovementPlusPlus;
import fr.skytasul.guardianbeam.Laser;
import net.punchtree.util.color.PunchTreeColor;
import net.punchtree.util.debugvar.DebugVars;
import net.punchtree.util.particle.ParticleShapes;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

public class TargetGrapple implements Listener {

    // NOTE this might cause an error depending on how this plugin is loaded (if it's loaded before the Quarantine world is - not sure if this is caused by load property of plugin.yml)
    // facing negative x (x axis = 1141)
    private static Location TEST_TARGET_LOCATION;
    private static final Vector TEST_TARGET_FACING_DIRECTION = new Vector(-1, 0, 0);
    private final GrappleTarget TEST_TARGET;
    private static final ItemStack TEST_TARGET_ITEM = new ItemStack(Material.LEATHER_CHESTPLATE);
    static {
        TEST_TARGET_ITEM.editMeta(meta -> meta.setCustomModelData(200));
    }
    public static final int TARGET_RADIUS = 1;


    public void cancelTask() {
        if (projectileCalculationTask != null) {
            projectileCalculationTask.cancel();
        }
    }

    static final class GrappleTarget {
        private final Location location;
        private final Vector facingDirection;
        private final PunchTreeColor color;
        private final Set<Player> grapplers = new HashSet<>();

        private static double GRAPPLE_SPEED = 1;
        private static double VELOCITY_MULTX = 0.2;

        GrappleTarget(Location location, Vector facingDirection, PunchTreeColor color) {
            this.location = location;
            this.facingDirection = facingDirection;
            this.color = color;
        }

        public void startGrapple(Player grappler) {
            GRAPPLE_SPEED = DebugVars.getDecimalAsDouble("grapple_speed", GRAPPLE_SPEED);
            VELOCITY_MULTX = DebugVars.getDecimalAsDouble("grapple_velocity_multx", VELOCITY_MULTX);
            if (grapplers.contains(grappler)) return;

            grapplers.add(grappler);

            Vector direction = location.toVector().subtract(getChestLocation(grappler).toVector());
            double distance = direction.length();
            direction.normalize();
            // Round off the last little bit - a bit of inaccuracy in stopping should be more than fine
            int steps = (int) (distance / GRAPPLE_SPEED);
            Vector targetLocation = getChestLocation(grappler).toVector();
            Vector velocityStep = direction.multiply(GRAPPLE_SPEED);

            Laser.GuardianLaser laser = null;
            try {
                laser = new Laser.GuardianLaser(location, grappler, steps, 100);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
            laser.durationInTicks();
            laser.start(MovementPlusPlus.getPlugin());

            new BukkitRunnable() {
                int i = 0;
                public void run() {
                    targetLocation.add(velocityStep);
                    // TODO network jitter can cause the magnitude of this difference vector to suddenly be very large - cap a maximum on it's magnitude
                    Location chestLocation = getChestLocation(grappler);
                    grappler.setVelocity(targetLocation.clone().subtract(chestLocation.toVector()).multiply(VELOCITY_MULTX));
//                    ParticleShapes.setParticleBuilder(particleBuilder);
//                    ParticleShapes.spawnParticleLine(chestLocation, location, (int) (location.toVector().subtract(chestLocation.toVector()).length() * DebugVars.getDecimalAsFloat("grapple_particle_line_steps", 5f)));
//                    spawnSwellLine(chestLocation, location, (int) (location.toVector().subtract(chestLocation.toVector()).length() * DebugVars.getDecimalAsFloat("grapple_particle_line_steps", 5f)));
                    ++i;
                    if ( i == steps ) {
                        grapplers.remove(grappler);
                        cancel();
                    }
                }

                void spawnSwellLine(Location a, Location b, int steps) {
                    Vector difference = b.clone().toVector().subtract(a.toVector());
                    double length = difference.length();
                    difference.multiply(1.0 / (double)(steps - 1));

                    ParticleBuilder particleBuilder = new ParticleBuilder(Particle.REDSTONE).color(color.getBukkitColor());

                    for(int i = 0; i < steps; ++i) {
                        Location l = a.clone().add(difference.clone().multiply(i));
                        double position = l.toVector().subtract(a.toVector()).length();
                        double percent = Math.max(0.01, Math.abs(position - (length / 2.0)) / (length / 2.0)) + 0.25;
                        particleBuilder.data(new Particle.DustOptions(color.getBukkitColor(), (float) percent)).location(l).spawn();
                    }

                }
            }.runTaskTimer(MovementPlusPlus.getPlugin(), 0, 1);
        }

        float smoothstep (float edge0, float edge1, float x)
        {
            if (x < edge0)
                return 0;

            if (x >= edge1)
                return 1;

            // Scale/bias into [0..1] range
            x = (x - edge0) / (edge1 - edge0);

            return x * x * (3 - 2 * x);
        }

        public Location location() {
            return location;
        }

        public Vector facingDirection() {
            return facingDirection;
        }

        public PunchTreeColor color() {
            return color;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (GrappleTarget) obj;
            return Objects.equals(this.location, that.location) &&
                    Objects.equals(this.facingDirection, that.facingDirection) &&
                    Objects.equals(this.color, that.color);
        }

        @Override
        public int hashCode() {
            return Objects.hash(location, facingDirection, color);
        }

        @Override
        public String toString() {
            return "GrappleTarget[" +
                    "location=" + location + ", " +
                    "facingDirection=" + facingDirection + ", " +
                    "color=" + color + ']';
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


    private static Location getChestLocation(Player player) {
        return player.getEyeLocation().add(player.getLocation()).multiply(0.5);
    }

    public TargetGrapple() {
        TEST_TARGET_LOCATION = new Location(Bukkit.getWorld("Quarantine"), 1141, 76, -2971, 90, 0);
        TEST_TARGET = new GrappleTarget(TEST_TARGET_LOCATION, TEST_TARGET_FACING_DIRECTION, new PunchTreeColor(0, 200, 200));
        projectileCalculationTask = Bukkit.getScheduler().runTaskTimer(Bukkit.getPluginManager().getPlugin("MovementPlusPlus"), () -> {
            Iterator<TrackedArrow> iterator = trackedArrows.iterator();
            while(iterator.hasNext()) {
                TrackedArrow trackedArrow = iterator.next();
                Arrow arrow = trackedArrow.arrow;
                Location lastLocation = trackedArrow.lastLocation;
                Location currentLocation = arrow.getLocation();
                Vector lastDifference = TEST_TARGET.location().toVector().subtract(lastLocation.toVector());
                Vector currentDifference = TEST_TARGET.location().toVector().subtract(currentLocation.toVector());
                if (lastDifference.dot(TEST_TARGET.facingDirection()) < 0 && currentDifference.dot(TEST_TARGET.facingDirection()) >= 0) {

                    // Calculate plane intersection
                    Vector arrowDirection = currentLocation.toVector().subtract(lastLocation.toVector());
                    Vector arrowToTarget = TEST_TARGET.location().toVector().subtract(lastLocation.toVector());
                    double t = arrowToTarget.dot(TEST_TARGET.facingDirection()) / arrowDirection.dot(TEST_TARGET.facingDirection());
                    // NOTE MUTATING LAST LOCATION FOR EFFICIENCY - NOT VALID AFTER THIS POINT
                    Location intersection = lastLocation.add(arrowDirection.multiply(t));

                    double intersectionDistance = intersection.distance(TEST_TARGET.location());

                    double playerDistance = intersection.distance(trackedArrow.shooter.getLocation());

                    if (intersectionDistance < TARGET_RADIUS) {
                        trackedArrow.shooter.sendMessage("Target hit detected (shot by " + ((Player) arrow.getShooter()).getName() + ")");
                        trackedArrow.shooter.playSound(trackedArrow.shooter.getLocation(), Sound.ITEM_TRIDENT_HIT_GROUND, (float) (5. / Math.max(playerDistance / 5, 1)), 1);
                        trackedArrow.shooter.playSound(trackedArrow.shooter.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 15, 1);
                        TEST_TARGET.location().getWorld().playSound(intersection, Sound.BLOCK_WOOL_HIT, 2, 1);
                        TEST_TARGET.location().getWorld().spawnParticle(Particle.BLOCK_DUST, intersection, 50, Bukkit.createBlockData(Material.BONE_BLOCK));

                        arrow.remove();

                        TEST_TARGET.startGrapple(trackedArrow.shooter);
                    } else {
                        trackedArrow.shooter.sendMessage("Plane hit detected (distance " + String.format("%.03f", intersectionDistance) + ")");
                    }

                    iterator.remove();
                } else if (arrow.getTicksLived() > 200 || !arrow.isTicking()) {
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
        if ( ! MovementPlusPlus.isMovementPlayer(player)) { return; }

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
