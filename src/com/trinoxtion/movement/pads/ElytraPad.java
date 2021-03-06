package com.trinoxtion.movement.pads;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import com.trinoxtion.movement.MovementPlayer;
import com.trinoxtion.movement.MovementPlusPlus;

public final class ElytraPad extends JumpPad implements Listener{
	
	public ElytraPad(Material material, JumpPad jp){
		this(material, jp.getHeight(), jp.getCost(), jp.isActive());
	}
	
	public ElytraPad(Material material, float height, float cost, boolean active) {
		super(material, height, cost, active);
		MovementPlusPlus.registerEvents(this);
	}
	
	public static final ElytraPad ELYTRA_PAD = new ElytraPad(Material.PINK_GLAZED_TERRACOTTA, JumpPad.STRONG_TRAMPOLINE);
	
	@Override
	public void onMovement(PlayerMoveEvent event, MovementPlayer movementPlayer) {
		resetElytraIfOnGround(movementPlayer);
		
		if(!this.accept(event.getTo())) return;
		
		// We extend Jumppad, so call parent onMovement() func to initiate jumping
		super.onMovement(event, movementPlayer);	
		
		giveElytraIfJumping(movementPlayer);
	}
	
	@Override
	public void onQuit(MovementPlayer movementPlayer){
		resetElytra(movementPlayer);
	}
	
	@Override
	public void onDeath(MovementPlayer movementPlayer){
		resetElytra(movementPlayer);
	}
	
	private static void giveElytraIfJumping(MovementPlayer movementPlayer) {
		Player player = movementPlayer.getPlayer();
		if (movementPlayer.isJumping() && !ElytraManager.hasElytra(player)){
			giveElytra(player);
		}
	}
	
	private static void giveElytra(Player player) {
		ItemStack chestplateToSave = player.getInventory().getChestplate();
		
		ElytraManager.playersWithElytra.put(player.getUniqueId(), chestplateToSave);
		
		player.getInventory().setChestplate(new ItemStack(Material.ELYTRA));
	}
	
	private void resetElytraIfOnGround(MovementPlayer movementPlayer){
		Player player = movementPlayer.getPlayer();
		if (player.isOnGround()) {
			resetElytra(player);
		}
	}
	
//	!playerIsOnKeepBlock(player)
//	private boolean playerIsOnKeepBlock(MovementPlayer movementPlayer) {
//		
//	}
	
	private static void resetElytra(MovementPlayer movementPlayer) {
		resetElytra(movementPlayer.getPlayer());
	}
	private static void resetElytra(Player player){
		if (ElytraManager.hasElytra(player)){
			removeElytra(player);
		}
	}
	
	private static void removeElytra(Player player) {
		player.getInventory().setChestplate(ElytraManager.playersWithElytra.get(player.getUniqueId()));
		ElytraManager.playersWithElytra.remove(player.getUniqueId());
	}
	
}
