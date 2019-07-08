package com.trinoxtion.movement.pads;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.trinoxtion.movement.MovementPlayer;
import com.trinoxtion.movement.MovementPlusPlus;

public class HealthPad extends SimplePad implements Listener {

	public static final HealthPad DEFAULT = new HealthPad(Material.CHISELED_QUARTZ_BLOCK);
	
	private static final double DEFAULT_RATE_IN_SECONDS = 0.6;
	
	private BukkitTask healthTask;
	private final long rateInTicks;
	private Set<MovementPlayer> playersOnPads = new HashSet<>();
	
	public HealthPad(Material material) {
		this(material, DEFAULT_RATE_IN_SECONDS);
	}
	
	public HealthPad(Material material, double rateInSeconds){
		super(material);
		this.rateInTicks = (long) (rateInSeconds * 20);

		MovementPlusPlus.registerEvents(this);
		
		startHealthTask();
	}
	
	public void startHealthTask() {
		if (healthTask == null) {
			healthTask = new BukkitRunnable(){
				public void run(){
					doHealthPadTick();
				}	
			}.runTaskTimerAsynchronously(MovementPlusPlus.getPlugin(), rateInTicks / 2, rateInTicks);
		}
	}
	
	public void stopHealthTask() {
		if (healthTask != null) {
			healthTask.cancel();
			healthTask = null;
		}
	}
	
	private void doHealthPadTick() {
		playersOnPads.forEach(this::incrementHealth);
	}
	
	private void incrementHealth(MovementPlayer movementPlayer) {
		movementPlayer.getPlayer().setHealth(Math.min(movementPlayer.getPlayer().getHealth() + 0.5, 20));
	}
	
	@Override
	public void onMovement(PlayerMoveEvent event, MovementPlayer mp) {
		if(this.accept(event.getTo())) {
			playersOnPads.add(mp);
		} else {
			playersOnPads.remove(mp);
		}
	}
	
	@Override
	public void onQuit(MovementPlayer mp){
		playersOnPads.remove(mp);
	}
	
	@Override
	public void onDeath(MovementPlayer mp){
		playersOnPads.remove(mp);
	}

}

