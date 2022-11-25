package com.trinoxtion.movement.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.trinoxtion.movement.MovementPlusPlus;
import com.trinoxtion.movement.MovementSystem;

public class MVTestCommand implements CommandExecutor {

	private MovementSystem testMovementSystem = MovementPlusPlus.CXOMS_MOVEMENT;

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		if (!(sender instanceof Player player)) {
			return true;
		}

		if (label.equalsIgnoreCase("mvtest")) {
			testMovementSystem.addPlayer(player);
			player.sendMessage(ChatColor.BLUE + "Something");
		} else if (label.equalsIgnoreCase("mvclear")) {
			testMovementSystem.removePlayer(player);
			player.sendMessage(ChatColor.RED + "Something");
		}

		return true;
	}
	
}
