package com.trinoxtion.movement.launchers;

import com.destroystokyo.paper.ParticleBuilder;
import net.punchtree.util.particle.ParticleShapes;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.trinoxtion.movement.MovementPlayer;
import com.trinoxtion.movement.MovementPlusPlus;
import com.trinoxtion.movement.pads.SimplePad;

public class Launcher {

	private final Vector launchDirection;
	private final double launchPower;
	private final boolean launchAdd;
	
	public Launcher(Vector launchDirection, double launchPower, boolean launchAdd) {
		this.launchDirection = launchDirection;
		this.launchPower = launchPower;
		this.launchAdd = launchAdd;
	}
	
	public void launch(MovementPlayer mp, Location launchLocation) {
		Player player = mp.getPlayer();
		Vector launchVector = launchDirection.clone().multiply(launchPower);
		if (launchAdd) launchVector.add(player.getVelocity());
		player.setVelocity(player.getVelocity().clone().setY(1));
		
		playLaunchPolish(player, launchLocation.clone().subtract(0, SimplePad.BLOCK_BELOW_DETECTION_RANGE, 0).getBlock());
		
		new BukkitRunnable() {
			int i = 0;
			public void run() {
				player.setVelocity(launchVector);
				if (i >= 1) {					
					this.cancel();
				}
				++i;
			}
			// We changed this from run task timer delay 0 period 1, is that because that didn't make sense? remove i check?
		}.runTaskLater(MovementPlusPlus.getPlugin(), 2);
		mp.setJumping();
	}
	
	private void playLaunchPolish(Player player, Block launcher) {
		//Sounds
		Location location = player.getLocation();
		player.getWorld().playSound(location, Sound.ENTITY_GHAST_SHOOT, .7f, 2f);
//		player.getWorld().playSound(location, Sound.BLOCK_ENDER_CHEST_OPEN, .7f, 2f);
		player.getWorld().playSound(location, Sound.ENTITY_ARROW_HIT, 1f, 1.4f);
		player.getWorld().playSound(location, Sound.ENTITY_GOAT_LONG_JUMP, 1f, .8f);
//		walljumpLocation.getWorld().playSound(walljumpLocation, Sound.UI_TOAST_IN, 1.6f, 1.5f);
		
		//Particles
		Location aa = launcher.getLocation().add(0, 1, 0);
		Location ab = launcher.getLocation().add(0, 1, 1);
		Location ba = launcher.getLocation().add(1, 1, 0);
		Location bb = launcher.getLocation().add(1, 1, 1);
		int steps = 17;
		ParticleBuilder particleBuilder = new ParticleBuilder(Particle.GLOW);
		ParticleShapes.setParticleBuilder(particleBuilder);
		ParticleShapes.drawQuad(aa, ab, ba, bb, steps);
		new BukkitRunnable() {
			int i = 0;
			public void run() {
				++i;
				aa.add(0, 0.1, 0);
				ab.add(0, 0.1, 0);
				ba.add(0, 0.1, 0);
				bb.add(0, 0.1, 0);
				ParticleShapes.setParticleBuilder(particleBuilder);
				ParticleShapes.drawQuad(aa, ab, ba, bb, steps - i * 3);
				if (i >= 2) this.cancel();
			}
		}.runTaskTimer(MovementPlusPlus.getPlugin(), 1, 1);
	}
}
