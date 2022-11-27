package com.trinoxtion.movement;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class MovementSystem implements Listener {
	
	/* Default Movement System - Nothing enabled */
	public static final MovementSystem DEFAULT = new MovementSystem(){
		@Override
		public void addPlayer(Player player){
			Bukkit.getLogger().warning("No movement system enabled!");
		}
		
	};
	
	private Set<MovementComponent> movementComponents = new HashSet<>();
	
	public MovementSystem(MovementComponent... movementComponents){
		this.movementComponents.addAll(Arrays.asList(movementComponents));
		MovementPlusPlus.registerEvents(this);
	}		
	
	//---------PLAYER MANAGEMENT-----------//
	public void addPlayer(Player player){
		MovementPlusPlus.registerPlayer(player, new MovementPlayer(player, this)); 
	}
	
	public void removePlayer(Player player) { removePlayer(player.getUniqueId()); }
	public void removePlayer(UUID uuid) {
		if (!MovementPlusPlus.isMovementPlayer(uuid)) return;
		
		removePlayer(MovementPlusPlus.getMovementPlayer(uuid));
	}
	
	private void removePlayer(MovementPlayer mp){
		movementComponents.forEach(mcs -> mcs.onQuit(mp));
		mp.restore();
		MovementPlusPlus.deregisterPlayer(mp.getUUID());
	}
	//-------------------------------------//
	
	public boolean hasComponent(MovementComponent movementComponent) {
		return movementComponents.contains(movementComponent);
	}
	
	/**
	 * Returns if an player uses this movement system
	 */
	private boolean usesThisSystem(Player p){ 
		return usesThisSystem(p.getUniqueId()); 
	}
	private boolean usesThisSystem(UUID uuid) {
		return MovementPlusPlus.isMovementPlayer(uuid) && usesThisSystem(MovementPlusPlus.getMovementPlayer(uuid));
	}
	private boolean usesThisSystem(MovementPlayer mp){
		return mp.getMovementSystem() == this;
	}
	
	//---------------EVENTS------------------//
	/*
	 * The following events dispatch to the system's components as appropriate
	 * Note, we use these events because they are most common, and because
	 * movement is incredibly frequent. Other events are entirely usable
	 * */
	
	//TODO this is not extensible. Just use move events in future?
	@EventHandler
	public void onMovement(PlayerMoveEvent e){
		if (usesThisSystem(e.getPlayer())){
			MovementPlayer mp = MovementPlusPlus.getMovementPlayer(e.getPlayer());
			
			movementComponents.forEach(mcs -> mcs.onMovement(e, mp));
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e){
		if (usesThisSystem(e.getPlayer())) {
			MovementPlayer mp = MovementPlusPlus.getMovementPlayer(e.getPlayer());
			removePlayer(mp);
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		onDeath(e.getEntity());
	}

	public void onDeath(@NotNull Player player) {
		if (usesThisSystem(player.getUniqueId())) {
			MovementPlayer mp = MovementPlusPlus.getMovementPlayer(player);

			movementComponents.forEach(mcs -> mcs.onDeath(mp));
		}
	}
	
}
