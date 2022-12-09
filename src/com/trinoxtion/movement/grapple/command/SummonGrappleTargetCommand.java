package com.trinoxtion.movement.grapple.command;

import com.trinoxtion.movement.grapple.GrappleTargetManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SummonGrappleTargetCommand implements CommandExecutor {

    private final GrappleTargetManager grappleTargetManager;

    public SummonGrappleTargetCommand(GrappleTargetManager grappleTargetManager) {
        this.grappleTargetManager = grappleTargetManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if ( ! (sender instanceof Player player)) return true;

        grappleTargetManager.summonGrappleTarget(player);

        return true;
    }
}
