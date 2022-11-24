package com.trinoxtion.movement.endzone;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import com.trinoxtion.movement.MovementComponent;
import com.trinoxtion.movement.MovementPlayer;

import net.md_5.bungee.api.ChatColor;

public class EndZoneRespawner implements MovementComponent {

	// TODO this doesn't play well with Rabbit becuase you can waste time falling off the edge
	// Need an event that occurs on endzone respawn so that rabbit can listen to that and drop the flag at respawn location

	private Map<UUID, Location> playersToLastPlaceOnGround = new WeakHashMap<>();
	
	private static final int MAX_HEALTH_PENALTY = 6; // 3 hearts
	
	@Override
	public void onMovement(PlayerMoveEvent event, MovementPlayer mp) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		if ( isInEndzone(event.getTo()) && playersToLastPlaceOnGround.containsKey(uuid)) {
			respawnAtLastPlaceOnGround(player);
		} else if ( isOnGround(event.getTo()) ) {
			playersToLastPlaceOnGround.put(uuid, event.getTo());
		} else if ( isInEndzone(event.getTo())) {
			Bukkit.broadcastMessage("Movement debug message - " + ChatColor.RED + "Player entered endzone without having ever been on the ground??");
		}
	}

	private void respawnAtLastPlaceOnGround(Player player) {
		player.setVelocity(new Vector(0,0,0));
		Location lastPlaceOnGround = playersToLastPlaceOnGround.get(player.getUniqueId());
		lastPlaceOnGround.setPitch(player.getLocation().getPitch());
		lastPlaceOnGround.setYaw(player.getLocation().getYaw());
		player.teleport(lastPlaceOnGround);
		player.setHealth(Math.max(1, player.getHealth() - MAX_HEALTH_PENALTY));
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_HURT, 1, .9f);
	}

	private boolean isOnGround(Location location) {
		return location.clone().subtract(0, 0.1, 0).getBlock().getType().isSolid();
	}
	
	private boolean isInEndzone(Location location) {
		return location.getBlock().getType() == Material.STRUCTURE_VOID;
	}
	
	@Override
	public void onQuit(MovementPlayer mp) {
		removeLastPlaceOnGroundTracking(mp);
	}
	
	@Override
	public void onDeath(MovementPlayer mp) {
		removeLastPlaceOnGroundTracking(mp);
	}
	
	private void removeLastPlaceOnGroundTracking(MovementPlayer player) {
		playersToLastPlaceOnGround.remove(player.getUUID());
	}
	
}
