package com.trinoxtion.movement.pads;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ElytraManager {

	static Map<UUID, ItemStack> playersWithElytra = new HashMap<>(); 
	
	static boolean hasElytra(Player player) {
		return playersWithElytra.containsKey(player.getUniqueId());
	}
	
}
