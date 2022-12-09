package com.trinoxtion.movement;

import java.util.*;

import com.trinoxtion.movement.command.MVTestCommand;
import com.trinoxtion.movement.editing.EditorWandListener;
import com.trinoxtion.movement.grapple.GrappleFacingDirection;
import com.trinoxtion.movement.grapple.GrappleTarget;
import com.trinoxtion.movement.grapple.GrappleTargetManager;
import com.trinoxtion.movement.grapple.TargetGrappling;
import com.trinoxtion.movement.grapple.command.GrappleTargetWandCommand;
import com.trinoxtion.movement.grapple.command.SummonGrappleTargetCommand;
import com.trinoxtion.movement.grapple.editing.GrappleTargetEditor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
import org.bukkit.util.Vector;

public class MovementPlusPlus extends JavaPlugin{

	private static Plugin plugin;

	public static Plugin getPlugin(){ return plugin; }
	public static MovementSystem CXOMS_MOVEMENT;

	private TargetGrappling targetGrapple;
	private GrappleTargetManager grappleTargetManager;

	public void onEnable(){
		plugin = this;
		new BukkitRunnable(){
			public void run(){
				for(MovementPlayer mp : mPlayers.values()) {
					mp.setStamina(mp.getStamina() + .5f);
				}
			}
		}.runTaskTimer(getPlugin(), 12, 12);

		Location TEST_TARGET_LOCATION = new Location(Bukkit.getWorld("Quarantine"), 1140.94999, 76, -2971, 90, 0);
		GrappleTarget TEST_TARGET = new GrappleTarget(TEST_TARGET_LOCATION, GrappleFacingDirection.WEST); // previously 0, 200, 200

		Location TEST_TARGET_2_LOCATION = new Location(Bukkit.getWorld("Quarantine"), 1140.94999, 76, -2975, 90, 0);
		GrappleTarget TEST_TARGET_2 = new GrappleTarget(TEST_TARGET_2_LOCATION, GrappleFacingDirection.WEST);

		Location TEST_TARGET_3_LOCATION = new Location(Bukkit.getWorld("Quarantine"), 1134, 76, -2990.94999, 0, 0);
		GrappleTarget TEST_TARGET_3 = new GrappleTarget(TEST_TARGET_3_LOCATION, GrappleFacingDirection.SOUTH);

		targetGrapple = new TargetGrappling(Set.of(TEST_TARGET, TEST_TARGET_2, TEST_TARGET_3));
		Bukkit.getPluginManager().registerEvents(targetGrapple, this);

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
				targetGrapple,
				new EndZoneRespawner()
		);

		MVTestCommand mvtestCommand = new MVTestCommand();
		getCommand("mvtest").setExecutor(mvtestCommand);
		getCommand("mvclear").setExecutor(mvtestCommand);

		grappleTargetManager = new GrappleTargetManager();
		getCommand("summon-grapple-target").setExecutor(new SummonGrappleTargetCommand(grappleTargetManager));
		getCommand("grapple-wand").setExecutor(new GrappleTargetWandCommand());

		GrappleTargetEditor grappleTargetEditor = new GrappleTargetEditor();
		Bukkit.getPluginManager().registerEvents(new EditorWandListener(grappleTargetEditor), this);
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
