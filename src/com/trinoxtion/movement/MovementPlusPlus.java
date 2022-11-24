package com.trinoxtion.movement;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.trinoxtion.movement.grapple.TargetGrapple;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.trinoxtion.movement.endzone.EndZoneRespawner;
import com.trinoxtion.movement.jumping.Walljump;
import com.trinoxtion.movement.launchers.Launchers;
import com.trinoxtion.movement.pads.ElytraLedge;
import com.trinoxtion.movement.pads.HealthPad;
import com.trinoxtion.movement.pads.JumpPad;

public class MovementPlusPlus extends JavaPlugin{

	private static Plugin plugin;
	private TargetGrapple targetGrapple;

	public static Plugin getPlugin(){ return plugin; }
	public static MovementSystem CXOMS_MOVEMENT;
	
	public void onEnable(){
		plugin = this;
		CXOMS_MOVEMENT = new MovementSystem(
				PlayerHealthStabilization.PLAYER_HEALTH_STABILIZATION,
				Walljump.WALLJUMP,
				JumpPad.LIGHT_JUMPPAD,
				JumpPad.MEDIUM_JUMPPAD,
				JumpPad.STRONG_JUMPPAD,
				JumpPad.LIGHT_TRAMPOLINE,
				JumpPad.MEDIUM_TRAMPOLINE,
				JumpPad.STRONG_TRAMPOLINE,
				ElytraLedge.ELYTRA_LEDGE,
				HealthPad.DEFAULT,
				Launchers.DEFAULT,
				new EndZoneRespawner()
		);
		new BukkitRunnable(){
			public void run(){
				for(MovementPlayer mp : mPlayers.values())
				mp.setStamina(mp.getStamina() + .5f);
			}
		}.runTaskTimer(getPlugin(), 12, 12);

		targetGrapple = new TargetGrapple();
		Bukkit.getPluginManager().registerEvents(targetGrapple, this);
	}
	
	public void onDisable(){
		for(MovementPlayer mp : mPlayers.values()){
			mp.restore();
			deregisterPlayer(mp.getPlayer());
		}

		targetGrapple.cancelTask();
	}
	
	public static Map<UUID, MovementPlayer> mPlayers = new HashMap<>();
	
	public static void registerPlayer(Player player, MovementPlayer mp){
		mPlayers.put(player.getUniqueId(), mp);
	}
	
	public static void deregisterPlayer(Player player){ deregisterPlayer(player.getUniqueId()); }
	public static void deregisterPlayer(UUID uuid){
		mPlayers.remove(uuid);
	}
	
	public static boolean isMovementPlayer(Player player){ return isMovementPlayer(player.getUniqueId()); }
	public static boolean isMovementPlayer(UUID uuid){
		return mPlayers.get(uuid) != null;
	}
	
	public static MovementPlayer getMovementPlayer(Player player){ return getMovementPlayer(player.getUniqueId()); }
	public static MovementPlayer getMovementPlayer(UUID uuid){
		return mPlayers.get(uuid);
	}
	
	public static void registerEvents(Listener listener	){
		Bukkit.getServer().getPluginManager().registerEvents(listener, getPlugin());
	}
	
}
