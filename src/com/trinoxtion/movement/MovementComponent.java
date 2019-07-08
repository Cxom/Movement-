package com.trinoxtion.movement;

import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

public interface MovementComponent {
	
	public void onMovement(PlayerMoveEvent event, MovementPlayer mp);
	
	default void onQuit(MovementPlayer mp) {}
	
	default void onDeath(MovementPlayer mp) {}
	
	default boolean usesThisComponent(Player player){
		return usesThisComponent(player.getUniqueId());
	}
	
	default boolean usesThisComponent(UUID uuid){
		return MovementPlusPlus.isMovementPlayer(uuid)
			&& MovementPlusPlus.getMovementPlayer(uuid).getMovementSystem().hasComponent(this);
	}
	
}
