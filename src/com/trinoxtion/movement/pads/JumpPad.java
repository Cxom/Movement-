package com.trinoxtion.movement.pads;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import com.trinoxtion.movement.MovementPlayer;

public class JumpPad extends SimplePad {

	public static final JumpPad LIGHT_JUMPPAD = new JumpPad(Material.LIME_TERRACOTTA, 11, 6, false);
	public static final JumpPad MEDIUM_JUMPPAD = new JumpPad(Material.YELLOW_TERRACOTTA, 16, 11, false);
	public static final JumpPad STRONG_JUMPPAD = new JumpPad(Material.RED_TERRACOTTA, 21, 15, false);
	
	public static final JumpPad LIGHT_TRAMPOLINE = new JumpPad(Material.EMERALD_BLOCK, 11, 0, true);
	public static final JumpPad MEDIUM_TRAMPOLINE = new JumpPad(Material.GOLD_BLOCK, 16, 0, true);
	public static final JumpPad STRONG_TRAMPOLINE = new JumpPad(Material.REDSTONE_BLOCK, 21, 0, true);
	
	private float height;
	private float lateral;
	private float cost;
	
	private boolean active;
	
	public JumpPad(Material material, float height, float cost, boolean active) {
		super(material);
		this.height = height;
		this.lateral = 1 + (height / 16);
		this.cost = cost;
		this.active = active;
	}
	
	public float getHeight(){
		return height;
	}
	
	public float getCost(){
		return cost;
	}
	
	public boolean isActive() {
		return active;
	}

	@Override
	public void onMovement(PlayerMoveEvent event, MovementPlayer mp) {
		/* The player, to be launched, is:
		 * 		1. Not Jumping
		 * 		
		 * 		2. On the ground, on a jumppad, and the jumppad is active 
		 * 		   OR
		 * 		   Has moved upward from a jumppad that was not active
		 * 		
		 * 		3. Has enough stamina
		 * */
		if (!mp.isJumping()
				&& 	  ((this.isActive()
						&& event.getTo().getY() <= event.getFrom().getY()
						&& this.accept(event.getTo()))
					|| (!this.isActive()
						&& event.getTo().getY() > event.getFrom().getY()
						&& this.accept(event.getFrom()))) 
				&& mp.setStamina(mp.getStamina() - cost)){
			Player player = mp.getPlayer();
			Vector vector = player.getVelocity();
			player.setVelocity(new Vector(vector.getX() * lateral, height / 9 , vector.getZ() * lateral));
			playJumpPolish(player);
			mp.setJumping();
		}
		
	}
	
	private void playJumpPolish(Player player) {
		//Sounds
		Location location = player.getLocation();
		player.getWorld().playSound(location, Sound.ENTITY_GHAST_SHOOT, .7f, 2f);
		player.getWorld().playSound(location, Sound.ENTITY_GOAT_LONG_JUMP, 1f, .8f);
//		walljumpLocation.getWorld().playSound(walljumpLocation, Sound.UI_TOAST_IN, 1.6f, 1.5f);
		
		//Particles
	}
	
}
