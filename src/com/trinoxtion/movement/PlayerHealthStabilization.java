package com.trinoxtion.movement;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerHealthStabilization implements MovementComponent, Listener {
	
	public static final PlayerHealthStabilization PLAYER_HEALTH_STABILIZATION = new PlayerHealthStabilization();
	
	private PlayerHealthStabilization(){
		super();
		MovementPlusPlus.registerEvents(this);
	}

	// These methods can each be encapsulated in a class later for flexibility, as need be 
	@EventHandler(priority = EventPriority.LOWEST)
	public void onFallDamage(EntityDamageEvent e){
		if (e.getEntity() instanceof Player
				&& usesThisComponent((Player) e.getEntity()) 
				&& e.getCause() == DamageCause.FALL){
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent e) {
		if (e.getEntity() instanceof Player 
				&& usesThisComponent((Player) e.getEntity())){
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onPlayerRegainHealth(EntityRegainHealthEvent e) {
		if (e.getEntity() instanceof Player
				&& usesThisComponent((Player) e.getEntity())){
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onDeathEvent(PlayerDeathEvent e){
		if (e.getEntity() instanceof Player){
			Player player = (Player) e.getEntity();
			if (usesThisComponent(player)){
				MovementPlusPlus.getMovementPlayer(player).setStamina(MovementPlayer.DEFAULT_FULL_STAMINA);
			}
		}
	}

	@Override
	public void onMovement(PlayerMoveEvent event, MovementPlayer mp) {
		// No movement involved
	}
	
}
