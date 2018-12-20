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
		this(material, (byte) -1, jp);
	}
	
	public ElytraPad(Material material, byte data, JumpPad jp){
		this(material, data, jp.getHeight(), jp.getCost(), jp.isActive());
	}
	
	public ElytraPad(Material material, float height, float cost, boolean active){
		this(material, (byte) -1, height, cost, active);
	}
	
	public ElytraPad(Material material, byte data, float height, float cost, boolean active) {
		super(material, data, height, cost, active);
		MovementPlusPlus.registerEvents(this);
	}
	
	public static final ElytraPad ELYTRA_PAD = new ElytraPad(Material.PINK_GLAZED_TERRACOTTA, JumpPad.STRONG_TRAMPOLINE);
	
	private static Map<UUID, ItemStack> elytrad = new HashMap<>(); 
	
//	@Deprecated
//	public static void elytraPad(MovementPlayer mp){
//		Player player = mp.getPlayer();
//		Vector vector = player.getVelocity();
//		if(player.getLocation().subtract(0, 1, 0).getBlock().getType() == ELYTRA_PAD){
//			player.setVelocity(new Vector(vector.getX() * 2, 16.0/9, vector.getZ() * 2));
//			if (!elytrad.containsKey(player.getUniqueId())){
//				elytrad.put(player.getUniqueId(), player.getInventory().getChestplate());	
//			}
//			player.getInventory().setChestplate(new ItemStack(Material.ELYTRA));
//			mp.setJumping();
//		}
//	}
	
	@Override
	public void onMovement(PlayerMoveEvent event, MovementPlayer mp) {
		if (event.getPlayer().isOnGround()) { reset(event.getPlayer()); }
		super.onMovement(event, mp);
		if (mp.isJumping() && !elytrad.containsKey(mp.getUUID())){
			elytrad.put(mp.getUUID(), mp.getPlayer().getInventory().getChestplate());
			mp.getPlayer().getInventory().setChestplate(new ItemStack(Material.ELYTRA));
		}
	}
	
//	@EventHandler(priority = EventPriority.LOW)
//	public void onLand(PlayerMoveEvent e){
//		if (e.getTo().getY() <= e.getFrom().getY()
//			&& e.getTo().clone().subtract(0, -.5, 0).getBlock().getType().isSolid()
//			&& !e.getPlayer().isGliding()){
//			reset(e.getPlayer());
//		}
//	}
	
	@Override
	public void onQuit(MovementPlayer mp){
		reset(mp.getPlayer());
	}
	
	@Override
	public void onDeath(MovementPlayer mp){
		reset(mp.getPlayer());
	}
	
	private static void reset(Player player){
		if (elytrad.containsKey(player.getUniqueId())){
			player.getInventory().setChestplate(elytrad.get(player.getUniqueId()));
			elytrad.remove(player.getUniqueId());
		}
	}
	
}
