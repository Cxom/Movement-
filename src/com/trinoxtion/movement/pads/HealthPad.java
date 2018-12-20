package com.trinoxtion.movement.pads;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.trinoxtion.movement.MovementPlayer;
import com.trinoxtion.movement.MovementPlusPlus;

public class HealthPad extends SimplePad implements Listener {

	public static final HealthPad DEFAULT = new HealthPad(Material.QUARTZ_BLOCK, (byte) 1);
	
	//This is for cancelling, but is it necessary or just cleaned on reload regardless? How is an async task time stopped?
	//TODO
	@SuppressWarnings("unused")
	private final BukkitTask healthTask;
	
	private final long rateInTicks;
	
	private Set<MovementPlayer> playersOnPads = new HashSet<>();
	
	public HealthPad(Material material){
		this(material, (byte) 0);
	}
	
	public HealthPad(Material material, byte data) {
		this(material, data, 0.6);
	}
	
	public HealthPad(Material material, byte data, double rateInSeconds){
		super(material, data);
		this.rateInTicks = (long) (rateInSeconds * 20);

		MovementPlusPlus.registerEvents(this);
		
		healthTask = new BukkitRunnable(){
			public void run(){
				playersOnPads.forEach(mp -> {
					mp.getPlayer().setHealth(Math.min(mp.getPlayer().getHealth() + 0.5, 20));
				});
			}	
		}.runTaskTimerAsynchronously(MovementPlusPlus.getPlugin(), rateInTicks / 2, rateInTicks);
	}
	
//	@EventHandler
//	public void onPlayerMove(PlayerMoveEvent e) {
//		Player player = e.getPlayer();
//		Bukkit.broadcastMessage(player.getName() + " uses this component: " + usesThisComponent(player));
//		if (usesThisComponent(player)){
//			MovementPlayer mp = MovementPlusPlus.getMovementPlayer(player);
//			if (player.getLocation().getBlock().getRelative(0, -1, 0).getType() == this.material) {
//				playersOnPads.add(mp);
//				Bukkit.broadcastMessage(player.getName() + " added to healthpad list");
//			} else {
//				playersOnPads.remove(mp);
//				Bukkit.broadcastMessage(player.getName() + " removed from healthpad list");
//			}
//		}
//	}
	
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

