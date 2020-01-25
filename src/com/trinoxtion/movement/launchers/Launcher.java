package com.trinoxtion.movement.launchers;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.trinoxtion.movement.MovementPlayer;

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
		player.setVelocity(launchVector);
		mp.setJumping();
	}

}
