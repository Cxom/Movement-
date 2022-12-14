package com.trinoxtion.movement.grapple;

import com.destroystokyo.paper.ParticleBuilder;
import com.trinoxtion.movement.MovementPlayer;
import com.trinoxtion.movement.MovementPlusPlus;
import net.punchtree.util.color.PunchTreeColor;
import net.punchtree.util.debugvar.DebugVars;
import net.punchtree.util.particle.ParticleShapes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class GrappleTarget {

    public static final PunchTreeColor DEFAULT_COLOR = new PunchTreeColor(0, 170, 255);
    static final ItemStack TARGET_ITEM = new ItemStack(Material.LEATHER_CHESTPLATE);
    static final ItemStack TARGET_ITEM_DOWN_45 = new ItemStack(Material.LEATHER_CHESTPLATE);
    static final ItemStack TARGET_ITEM_STRAIGHT_DOWN = new ItemStack(Material.LEATHER_CHESTPLATE);
    static final ItemStack TARGET_ITEM_UP_45 = new ItemStack(Material.LEATHER_CHESTPLATE);
    static final ItemStack TARGET_ITEM_STRAIGHT_UP = new ItemStack(Material.LEATHER_CHESTPLATE);
    static {
        TARGET_ITEM.editMeta(meta -> {
            meta.setCustomModelData(200);
            ((LeatherArmorMeta) meta).setColor(DEFAULT_COLOR.getBukkitColor());
        });
        TARGET_ITEM_DOWN_45.editMeta(meta -> {
            meta.setCustomModelData(201);
            ((LeatherArmorMeta) meta).setColor(DEFAULT_COLOR.getBukkitColor());
        });
        TARGET_ITEM_STRAIGHT_DOWN.editMeta(meta -> {
            meta.setCustomModelData(202);
            ((LeatherArmorMeta) meta).setColor(DEFAULT_COLOR.getBukkitColor());
        });
        TARGET_ITEM_UP_45.editMeta(meta -> {
            meta.setCustomModelData(203);
            ((LeatherArmorMeta) meta).setColor(DEFAULT_COLOR.getBukkitColor());
        });
        TARGET_ITEM_STRAIGHT_UP.editMeta(meta -> {
            meta.setCustomModelData(204);
            ((LeatherArmorMeta) meta).setColor(DEFAULT_COLOR.getBukkitColor());
        });
    }
    private static final int TARGET_RADIUS = 1;
    private static double GRAPPLE_SPEED = 1;
    private static double VELOCITY_MULTX = 0.2;

    private Location location;
    private Vector facingDirection;
    private final PunchTreeColor color;
    private final UUID armorStandUniqueId;
    private WeakReference<ArmorStand> cachedArmorStand;

    public GrappleTarget(Location location, GrappleFacingDirection facingDirection, UUID armorStandUniqueId) {
        this.location = location;
        this.facingDirection = facingDirection.getVector();
        this.facingDirection.normalize();
        this.color = DEFAULT_COLOR;
        this.armorStandUniqueId = armorStandUniqueId;
    }

    private final Map<MovementPlayer, GrappleInformation> grapplers = new HashMap<>();

    // TODO unit test this
    // TODO this entire function can be improved by using actual arrow travel direction
    // It is exceedingly rare for arrows to change X or Z components, and in those cases the function could just be called again
    // For all other components, it is possible to just immediately raycast, instead of doing a lot of dot product math
    // In the mean time, better to include too many targets, than too few, so this is just a to-do optimization
    public boolean canBeHitBy(Arrow arrow) {
        Vector direction = arrow.getVelocity().clone();

        // If the arrow is shot downward and its initial position is lower than the bottom edge of the target, it can't be hit
        // the center minus the radius is just a bound - targets that are level have the bottom edge that's the farthest from the center vertically -> the radius
        if (direction.getY() <= 0 && arrow.getLocation().getY() < location().getY() - radius()) {
            return false;
        }

        // We don't want to analyze y component because gravity will change it
        direction.setY(0);

        // If the arrow is shot straight upward or straight downward and the target's center position is greater than its radius away horizontally, it can't be hit
        if ( direction.getX() == 0 && direction.getZ() == 0) {
            // moreover, return here no matter what because direction will be zero after this (and not many arrows are likely to meet this criteria)
            return location().toVector().setY(0).distance(arrow.getLocation().toVector().setY(0)) <= radius();
        }

        boolean targetIsOrientedStraightVertically = facingDirection().equals(new Vector(0, 1, 0)) || facingDirection().equals(new Vector(0, -1, 0));
        // If the target is oriented vertically, then the direction vector with zerod y component will always be perpendicular to the target facing direction, meaning the dot product will equal zero!
        assert !targetIsOrientedStraightVertically || direction.dot(facingDirection()) == 0;
        if (targetIsOrientedStraightVertically && direction.dot(facingDirection()) != 0) {
            Bukkit.getLogger().severe("Assumption about dot product is wrong!!!");
        }
        // Following this logic, less than or EQUAL TO zero allows vertically oriented targets to not be culled
        boolean isGoingOppositeDirectionTargetIsFacing = direction.dot(facingDirection()) <= 0;
        boolean isTargetFacingUp = facingDirection.getY() > 0;

        boolean isPastTarget;
        if (targetIsOrientedStraightVertically) {
            // Since the up vector and the forward vector are the same, there is no naturally occurring cross product "right" vector
            // However, we can simply use the y plane of the target in this case
            // TODO THIS REQUIRES TESTING
            Vector directionProjectedOnTargetPlane = direction.clone().setY(0);
            Vector targetEdgeLocation = location().toVector().add(directionProjectedOnTargetPlane.normalize().multiply(radius()));
            Vector arrowToTargetEdgeProjectedOnTargetPlane = targetEdgeLocation.subtract(arrow.getLocation().toVector()).setY(0);
            isPastTarget = direction.dot(arrowToTargetEdgeProjectedOnTargetPlane) < 0;
        } else {
            Vector right = facingDirection().getCrossProduct(new Vector(0, 1, 0));
            assert right.isNormalized();
            Vector directionProjectedOntoRight = right.clone().multiply(direction.dot(right));
            // Add the target radius so we're actually checking against the target edge - otherwise, we'll cull shots where
            // we're past the target center along the right vector, but can still see less than half of the target
            // Note - normalization here does modify the projected vector magnitude, but we don't really care since all we're using it for is direction analysis (we only care about the sign)
            Vector arrowToTarget = location().toVector().add(directionProjectedOntoRight.normalize().multiply(radius())).subtract(arrow.getLocation().toVector());
            isPastTarget = directionProjectedOntoRight.dot(arrowToTarget) < 0;
        }

        return (isGoingOppositeDirectionTargetIsFacing || isTargetFacingUp) && !isPastTarget;
    }

    public double radius() {
        return TARGET_RADIUS;
    }

    public ArmorStand getArmorStand() {
        if (cachedArmorStand == null || cachedArmorStand.get() == null) {
            cachedArmorStand = new WeakReference<>((ArmorStand) Bukkit.getEntity(armorStandUniqueId));
        }
        return cachedArmorStand.get();
    }

    public void moveTo(Location newLocation) {
        this.location = newLocation;
        getArmorStand().teleport(this.location);
    }

    public void rotateTo(GrappleFacingDirection facingDirection) {
        this.facingDirection = facingDirection.getVector();
        ArmorStand armorStand = getArmorStand();
        armorStand.teleport(this.location.setDirection(this.facingDirection));
        armorStand.setItem(EquipmentSlot.HAND, facingDirection.getVerticalComponent().getTargetItem());
    }

    private record GrappleInformation(BukkitTask grappleTask) {}

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

        BukkitTask grappleTask = new BukkitRunnable() {
            private final double MAX_VELOCITY_MAGNITUDE = DebugVars.getDecimalAsDouble("grapple_max_velocity_magnitude", 5);
            private final double MAX_VELOCITY_MAGNITUDE_SQUARED = MAX_VELOCITY_MAGNITUDE * MAX_VELOCITY_MAGNITUDE;
            int i = 0;

            ParticleBuilder grappleParticle = new ParticleBuilder(Particle.REDSTONE).color(DEFAULT_COLOR.getBukkitColor());

            public void run() {
                //Set the laser before updating target location

                targetLocation.add(velocityStep);
                // TODO network jitter can cause the magnitude of this difference vector to suddenly be very large - cap a maximum on it's magnitude
                Location chestLocation = getChestLocation(player);
                Vector differenceToTarget = targetLocation.clone().subtract(chestLocation.toVector());
                // This works at capping the velocity, but because the pulling has its duration precalculated, it causes the player being stuck behind something to fail ungracefully
//                if (differenceToTarget.lengthSquared() > MAX_VELOCITY_MAGNITUDE_SQUARED) {
//                    differenceToTarget.normalize().multiply(MAX_VELOCITY_MAGNITUDE);
//                }
                player.setVelocity(differenceToTarget.multiply(VELOCITY_MULTX));
                ParticleShapes.setParticleBuilder(grappleParticle);
                ParticleShapes.spawnParticleLine(chestLocation, location, (int) (location.distance(chestLocation) * 5));

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

        grapplers.put(grappler, new GrappleInformation(grappleTask));
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

    // We don't implement equals or hashCode because location and facingDirection are mutable by the editing system
    // And we maintain a set in the target manager
    // Amd we aren't newing up grapple targets in random contexts

    @Override
    public String toString() {
        return "GrappleTarget[" +
                "location=" + location + ", " +
                "facingDirection=" + facingDirection + ", " +
                "color=" + color + ']';
    }

    private static Location getChestLocation(Player player) {
        Vector difference = player.getLocation().subtract(player.getEyeLocation()).toVector();
        return player.getEyeLocation().add(difference.multiply(0.3));
    }

    public void stopPlayerGrappling(MovementPlayer movementPlayer) {
        if (grapplers.containsKey(movementPlayer)) {
            GrappleInformation grappleInformation = grapplers.get(movementPlayer);
            grappleInformation.grappleTask.cancel();
            movementPlayer.setCurrentGrappleTarget(null);
            grapplers.remove(movementPlayer);
        }
    }
}
