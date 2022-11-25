package com.trinoxtion.movement;

import java.util.UUID;

import com.trinoxtion.movement.grapple.GrappleTarget;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class MovementPlayer{
	
	//-----Player---------------------------------//
	
	private final UUID uuid;

	private final MovementSystem ms;
	
	private final float xp;
	private final int xpLvl;
	private final boolean couldFly;
	
	MovementPlayer(Player player, MovementSystem ms){
		this.uuid = player.getUniqueId();
		this.ms = ms;
		this.xp = player.getExp();
		this.xpLvl = player.getLevel();
		this.couldFly = player.getAllowFlight();
		
		this.maxStamina = DEFAULT_FULL_STAMINA;
		this.stamina = maxStamina;
		this.setStamina(MovementPlayer.DEFAULT_FULL_STAMINA);
	}
	
	public MovementSystem getMovementSystem(){
		return ms;
	}
	
	public UUID getUUID() {
		return uuid;
	}	
	
	public Player getPlayer(){
		return Bukkit.getPlayer(uuid);
	}
	
	public void restore(){
		Player player = getPlayer();
		player.setExp(xp);
		player.setLevel(xpLvl);
		player.setLastDamage(xpLvl);
		player.setAllowFlight(couldFly);
	}
	
	//-----Stamina--------------------------------//
		
	public static final float DEFAULT_FULL_STAMINA = 16;
	
	public float maxStamina;
	
	private float stamina; //stored as a percent fraction out of 16
		
	public float getStamina(){
		return stamina;
	}
			
	public boolean setStamina(float stamina){
		if (stamina >= 0){
			this.stamina = Math.min(stamina, maxStamina);
			getPlayer().setExp((this.stamina / maxStamina) * .99F);
			getPlayer().setLevel((int) (this.stamina));
			return true;
		}
		return false;
	}
	
	public float getMaxStamina() {
		return maxStamina;
	}
	
	public void setMaxStamina(float maxStamina) {
		if (maxStamina > this.maxStamina) {
			float difference = maxStamina - this.maxStamina;
			this.maxStamina = maxStamina;
			setStamina(getStamina() + difference);
		} else if (maxStamina < this.maxStamina) {
			setStamina(Math.min(getStamina(), maxStamina));
			this.maxStamina = maxStamina;
		}
	}
		
	//-----Jumping--------------------------------//
	
	private boolean jumping;
	
	public boolean isJumping(){
		return jumping;
	}
	
	public void setJumping() {
		jumping = true;
		new BukkitRunnable(){
			public void run(){
				jumping = false;
			}
		}.runTaskLater(MovementPlusPlus.getPlugin(), 10 /*TODO Test, issues might result with a delay of only 1/20th of a sec*/);
	}

	private GrappleTarget currentGrappleTarget;
    public boolean isGrappling() {
		return currentGrappleTarget != null;
    }

	public void setCurrentGrappleTarget(GrappleTarget grappleTarget) {
		this.currentGrappleTarget = grappleTarget;
	}

	public void stopGrapple() {
		if (!isGrappling()) return;
		getPlayer().sendMessage("Stopping grappling");
		currentGrappleTarget.stopPlayerGrappling(this);
		currentGrappleTarget = null;
	}
}
