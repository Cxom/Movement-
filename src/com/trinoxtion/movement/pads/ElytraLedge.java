package com.trinoxtion.movement.pads;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import com.trinoxtion.movement.MovementPlayer;
import com.trinoxtion.movement.MovementPlusPlus;

public class ElytraLedge extends SimplePad implements Listener {

	private final List<Material> keepElytraBlocks;
	
	public ElytraLedge(Material material, List<Material> keepElytraBlocks) {
		super(material);
		this.keepElytraBlocks = new ArrayList<>(keepElytraBlocks);
		this.keepElytraBlocks.add(material);
		MovementPlusPlus.registerEvents(this);
	}
	
	public static final ElytraLedge ELYTRA_LEDGE = new ElytraLedge(Material.PINK_GLAZED_TERRACOTTA, 
														Arrays.asList(
																	JumpPad.STRONG_TRAMPOLINE.material,
																	JumpPad.MEDIUM_TRAMPOLINE.material,
																	JumpPad.LIGHT_TRAMPOLINE.material,
																	JumpPad.STRONG_JUMPPAD.material,
																	JumpPad.MEDIUM_JUMPPAD.material,
																	JumpPad.LIGHT_JUMPPAD.material));
	
	@Override
	public void onMovement(PlayerMoveEvent event, MovementPlayer movementPlayer) {
		resetElytraIfOnGround(movementPlayer);
		
		if(!this.accept(event.getTo())) return;
		
		giveElytra(movementPlayer);
	}
	
	@Override
	public void onQuit(MovementPlayer movementPlayer){
		resetElytra(movementPlayer);
	}
	
	@Override
	public void onDeath(MovementPlayer movementPlayer){
		resetElytra(movementPlayer);
	}
	
	private static void giveElytra(MovementPlayer movementPlayer) {
		Player player = movementPlayer.getPlayer();
		if (!ElytraManager.hasElytra(player)){
			ItemStack chestplateToSave = player.getInventory().getChestplate();
			
			ElytraManager.playersWithElytra.put(player.getUniqueId(), chestplateToSave);
			
			player.getInventory().setChestplate(new ItemStack(Material.ELYTRA));
		}
	}
	
	private void resetElytraIfOnGround(MovementPlayer movementPlayer){
		Player player = movementPlayer.getPlayer();
		if (player.isOnGround() && !isOnKeepElytraBlock(player)) {
			resetElytra(player);
		}
	}
	
	private boolean isOnKeepElytraBlock(Player player) {
		return keepElytraBlocks.contains(player.getLocation().clone().subtract(0, 1, 0).getBlock().getType());
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
