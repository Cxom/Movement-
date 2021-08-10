package com.trinoxtion.movement.launchers;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.trinoxtion.movement.MovementPlayer;
import com.trinoxtion.movement.MovementPlusPlus;

public class Launcher {

	private final Vector launchDirection;
	private final double launchPower;
	private final boolean launchAdd;
	
	public Launcher(Vector launchDirection, double launchPower, boolean launchAdd) {
		this.launchDirection = launchDirection;
		this.launchPower = launchPower;
		this.launchAdd = launchAdd;
	}
	
	public void launch(MovementPlayer mp) {
		Player player = mp.getPlayer();
		Vector launchVector = launchDirection.clone().multiply(launchPower);
		if (launchAdd) launchVector.add(player.getVelocity());
		player.setVelocity(player.getVelocity().clone().setY(1));
		new BukkitRunnable() {
			int i = 0;
			public void run() {
				player.setVelocity(launchVector);
				if (i >= 1) {					
					this.cancel();
				}
				++i;
			}
		}.runTaskTimer(MovementPlusPlus.getPlugin(), 0, 1);
		mp.setJumping();
	}

}
