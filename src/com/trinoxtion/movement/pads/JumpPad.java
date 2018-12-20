package com.trinoxtion.movement.pads;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import com.trinoxtion.movement.MovementPlayer;

public class JumpPad extends SimplePad {

	public static final JumpPad LIGHT_JUMPPAD = new JumpPad(Material.TERRACOTTA, (byte) 5, 11, 6, false);
	public static final JumpPad MEDIUM_JUMPPAD = new JumpPad(Material.TERRACOTTA, (byte) 4, 16, 11, false);
	public static final JumpPad STRONG_JUMPPAD = new JumpPad(Material.TERRACOTTA, (byte) 14, 21, 15, false);
	
	public static final JumpPad LIGHT_TRAMPOLINE = new JumpPad(Material.EMERALD_BLOCK, 11, 0, true);
	public static final JumpPad MEDIUM_TRAMPOLINE = new JumpPad(Material.GOLD_BLOCK, 16, 0, true);
	public static final JumpPad STRONG_TRAMPOLINE = new JumpPad(Material.REDSTONE_BLOCK, 21, 0, true);
	
	private float height;
	private float lateral;
	private float cost;
	
	private boolean active;
	
	public JumpPad(Material material, float height, float cost, boolean active){
		this(material, (byte) -1, height, cost, active);
	}
	
	public JumpPad(Material material, byte data, float height, float cost, boolean active) {
		super(material, data);
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
	
//	@Deprecated
//	public static void jumppad(MovementPlayer mp){
//		Player player = mp.getPlayer();
//		Location loc = player.getLocation();
//		Block block = loc.subtract(0, 1, 0).getBlock();
//		if (block.getType() == JUMP_PAD){
//			byte strength = block.getData();
//			float stamina = mp.getStamina();
//			Vector vector = player.getVelocity();
//			
//			if (stamina >= 15 && strength == strong){
//				player.setVelocity(new Vector(vector.getX() * 2, 16.0/9 , vector.getZ() * 2));// stamina = 16/16 * coef = 16 / 9
//				mp.setStamina(stamina - 15);
//			}else if (stamina >= 11 && strength == medium){
//				player.setVelocity(new Vector(vector.getX() * 7.0/4, 12.0/9 , vector.getZ() * 7.0/4));// stamina = 12/16 * coef = 12 / 9
//				mp.setStamina(stamina - 10);
//			}else if (stamina >= 6 && strength == light){
//				player.setVelocity(new Vector(vector.getX() * 6.0/4, 8.0/9 , vector.getZ() * 6.0/4));// stamina = 8/16 * coef
//				mp.setStamina(stamina - 5);
//			}else{
//				return;
//			}
//			vector = player.getVelocity();
//			mp.setJumping();
//		}
//				
//	}

	@Override
	public void onMovement(PlayerMoveEvent event, MovementPlayer mp) {
		/* The player is:
		 * 		- Not Jumping
		 * 		
		 * 		- On the ground, on a jumppad, and the jumppad is active 
		 * 		   OR
		 * 		- Has moved upward from a jumppad that was not active
		 * 		
		 * 		- Has enough stamina
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
			player.setVelocity(new Vector(vector.getX() * lateral, height / 9 , vector.getZ() * lateral));// stamina = 16/16 * coef = 16 / 9
//			Bukkit.broadcastMessage("After immedia: " + player.getVelocity().getY() + " - " + player.getName());
			mp.setJumping();
//			new BukkitRunnable(){
//				@Override
//				public void run() {
//					player.setVelocity(new Vector(player.getVelocity().getX(), height / 9, player.getVelocity().getZ()));// stamina = 16/16 * coef = 16 / 9
//				}
//			}.runTaskLater(MovementPlusPlus.getPlugin(), 2);
		}
		
	}
	
}
