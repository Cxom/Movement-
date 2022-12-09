package com.trinoxtion.movement.grapple.command;

import com.trinoxtion.movement.grapple.editing.GrappleTargetEditor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GrappleTargetWandCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if ( ! ( sender instanceof Player player )) return false;

        player.getInventory().addItem(GrappleTargetEditor.getNewWand());

        return true;
    }
}
