package com.trinoxtion.movement.jumping;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;

import com.trinoxtion.movement.MovementComponent;
import com.trinoxtion.movement.MovementPlayer;
import com.trinoxtion.movement.MovementPlusPlus;

public class Walljump implements MovementComponent, Listener {

	public static final Walljump WALLJUMP = new Walljump(); 
	
	private Walljump(){
		super();
		MovementPlusPlus.registerEvents(this);
	}
	
	@Override
	public void onMovement(PlayerMoveEvent event, MovementPlayer mp) {
		Player player = event.getPlayer();
		if (! (player.isFlying() || mp.getStamina() <=6) && canWalljump(event.getTo())){
			player.setAllowFlight(true);
		}else if (player.getGameMode() != GameMode.CREATIVE){
			player.setAllowFlight(false);
		}
	}

	@EventHandler
	public void onPlayerToggleFlight(PlayerToggleFlightEvent e) {
		Player player = e.getPlayer();
		if (player.getGameMode() != GameMode.CREATIVE && usesThisComponent(player)) {
			if (canWalljump(player.getLocation())) {
				MovementPlayer mp = MovementPlusPlus.getMovementPlayer(player);
				player.setSprinting(true);
				player.setVelocity(player.getEyeLocation().getDirection().multiply(.75).setY(.75));
				mp.setStamina(mp.getStamina() - 6);
			}
			e.setCancelled(true);
			player.setAllowFlight(false);
			player.setFlying(false);
		}
	}

	public static boolean canWalljump(Location loc) {
		World world = loc.getWorld();
		int x = (int) Math.floor(loc.getX());
		int y = (int) loc.getY();
		int z = (int) Math.floor(loc.getZ());
		Material south = world.getBlockAt (x,     	y,              	z + 1).getType();
		Material west = world.getBlockAt  (x - 1, 	y,              	z)	  .getType();
		Material north = world.getBlockAt (x,     	y,              	z - 1).getType();
		Material east = world.getBlockAt  (x + 1, 	y,              	z)	  .getType();
		Material south2 = world.getBlockAt(x,     	(int) (y + .5), 	z + 1).getType();
		Material west2 = world.getBlockAt (x - 1, 	(int) (y + .5), 	z)	  .getType();
		Material north2 = world.getBlockAt(x,     	(int) (y + .5), 	z - 1).getType();
		Material east2 = world.getBlockAt (x + 1, 	(int) (y + .5), 	z)	  .getType();
		if (south.isSolid()	&& south != Material.BARRIER
				&& south2.isSolid() && south2 != Material.BARRIER){
				return true;
		} else if (west.isSolid()	&& west != Material.BARRIER
				&& west2.isSolid() && west2 != Material.BARRIER){
				return true;
		} else if (north.isSolid()	&& north != Material.BARRIER
				&& north2.isSolid() && north2 != Material.BARRIER){
				return true;
		} else if (east.isSolid()	&& east != Material.BARRIER
				&& east2.isSolid() && east2 != Material.BARRIER){
				return true;
		}
		return false;
	}
	
}
