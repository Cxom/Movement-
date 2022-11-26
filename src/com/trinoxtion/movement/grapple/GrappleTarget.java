package com.trinoxtion.movement.grapple;

import com.destroystokyo.paper.ParticleBuilder;
import com.trinoxtion.movement.MovementPlayer;
import com.trinoxtion.movement.MovementPlusPlus;
import fr.skytasul.guardianbeam.Laser;
import net.punchtree.util.color.PunchTreeColor;
import net.punchtree.util.debugvar.DebugVars;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

public final class GrappleTarget {
    private static double GRAPPLE_SPEED = 1;
    private static double VELOCITY_MULTX = 0.2;

    private final Location location;
    private final Vector facingDirection;
    private final PunchTreeColor color;

    // TODO use movement player instead of player
    private final Map<MovementPlayer, GrappleInformation> grapplers = new HashMap<>();

    private record GrappleInformation(BukkitTask grappleTask, Laser laser) {}

    public GrappleTarget(Location location, Vector facingDirection, PunchTreeColor color) {
        this.location = location;
        this.facingDirection = facingDirection;
        this.color = color;
    }

    // TODO use movement player instead of player
    public void startGrapple(MovementPlayer grappler) {
        GRAPPLE_SPEED = DebugVars.getDecimalAsDouble("grapple_speed", GRAPPLE_SPEED);
        VELOCITY_MULTX = DebugVars.getDecimalAsDouble("grapple_velocity_multx", VELOCITY_MULTX);
        if (grapplers.containsKey(grappler)) return;
        if (grappler.isGrappling()) { // not grappling this board but another one
            grappler.stopGrapple();
        }

        Player player = grappler.getPlayer();

        Vector direction = location.toVector().subtract(getChestLocation(player).toVector());
        double distance = direction.length();
        direction.normalize();
        // Round off the last little bit - a bit of inaccuracy in stopping should be more than fine
        int steps = (int) (distance / GRAPPLE_SPEED);
        Vector targetLocation = getChestLocation(player).toVector();
        Vector velocityStep = direction.multiply(GRAPPLE_SPEED);

        Laser.GuardianLaser laser = null;
        try {
            laser = new Laser.GuardianLaser(location, player, steps, 100);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        laser.durationInTicks();
        laser.start(MovementPlusPlus.getPlugin());

        BukkitTask grappleTask = new BukkitRunnable() {
            private final double MAX_VELOCITY_MAGNITUDE = DebugVars.getDecimalAsDouble("grapple_max_velocity_magnitude", 5);
            private final double MAX_VELOCITY_MAGNITUDE_SQUARED = MAX_VELOCITY_MAGNITUDE * MAX_VELOCITY_MAGNITUDE;
            int i = 0;

            public void run() {
                targetLocation.add(velocityStep);
                // TODO network jitter can cause the magnitude of this difference vector to suddenly be very large - cap a maximum on it's magnitude
                Location chestLocation = getChestLocation(player);
                Vector differenceToTarget = targetLocation.clone().subtract(chestLocation.toVector());
                // This works at capping the velocity, but because the pulling has its duration precalculated, it causes the player being stuck behind something to fail ungracefully
//                if (differenceToTarget.lengthSquared() > MAX_VELOCITY_MAGNITUDE_SQUARED) {
//                    differenceToTarget.normalize().multiply(MAX_VELOCITY_MAGNITUDE);
//                }
                player.setVelocity(differenceToTarget.multiply(VELOCITY_MULTX));
//                    ParticleShapes.setParticleBuilder(particleBuilder);
//                    ParticleShapes.spawnParticleLine(chestLocation, location, (int) (location.toVector().subtract(chestLocation.toVector()).length() * DebugVars.getDecimalAsFloat("grapple_particle_line_steps", 5f)));
//                    spawnSwellLine(chestLocation, location, (int) (location.toVector().subtract(chestLocation.toVector()).length() * DebugVars.getDecimalAsFloat("grapple_particle_line_steps", 5f)));
                ++i;
                if (i == steps) {
                    grapplers.remove(grappler);
                    grappler.setCurrentGrappleTarget(null);
                    cancel();
                }
            }
        }.runTaskTimer(MovementPlusPlus.getPlugin(), 0, 1);

        grapplers.put(grappler, new GrappleInformation(grappleTask, laser));
        grappler.setCurrentGrappleTarget(this);
    }

    float smoothstep(float edge0, float edge1, float x) {
        if (x < edge0)
            return 0;

        if (x >= edge1)
            return 1;

        // Scale/bias into [0..1] range
        x = (x - edge0) / (edge1 - edge0);

        return x * x * (3 - 2 * x);
    }

    void spawnSwellLine(Location a, Location b, int steps) {
        Vector difference = b.clone().toVector().subtract(a.toVector());
        double length = difference.length();
        difference.multiply(1.0 / (double) (steps - 1));

        ParticleBuilder particleBuilder = new ParticleBuilder(Particle.REDSTONE).color(color.getBukkitColor());

        for (int i = 0; i < steps; ++i) {
            Location l = a.clone().add(difference.clone().multiply(i));
            double position = l.toVector().subtract(a.toVector()).length();
            double percent = Math.max(0.01, Math.abs(position - (length / 2.0)) / (length / 2.0)) + 0.25;
            particleBuilder.data(new Particle.DustOptions(color.getBukkitColor(), (float) percent)).location(l).spawn();
        }

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

    private static Location getChestLocation(Player player) {
        return player.getEyeLocation().add(player.getLocation()).multiply(0.5);
    }

    public void stopPlayerGrappling(MovementPlayer movementPlayer) {
        if (grapplers.containsKey(movementPlayer)) {
            GrappleInformation grappleInformation = grapplers.get(movementPlayer);
            grappleInformation.grappleTask.cancel();
            grappleInformation.laser.stop();
            movementPlayer.setCurrentGrappleTarget(null);
            grapplers.remove(movementPlayer);
        }
    }
}
