package com.trinoxtion.movement.grapple;

import com.destroystokyo.paper.ParticleBuilder;
import com.trinoxtion.movement.MovementPlusPlus;
import fr.skytasul.guardianbeam.Laser;
import net.punchtree.util.color.PunchTreeColor;
import net.punchtree.util.debugvar.DebugVars;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

final class GrappleTarget {
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
                if (i == steps) {
                    grapplers.remove(grappler);
                    cancel();
                }
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
        }.runTaskTimer(MovementPlusPlus.getPlugin(), 0, 1);
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
}
