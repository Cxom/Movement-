package com.trinoxtion.movement.jumping;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
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
	
	private static final int WALLJUMP_COST = 6;
	
	private static final double CB = 0.8; // Cardinal buffer
	private static final double OB = 0.66; // Ordinal buffer
	
	private Walljump(){
		super();
		MovementPlusPlus.registerEvents(this);
	}
	
	@Override
	public void onMovement(PlayerMoveEvent event, MovementPlayer mp) {
		Player player = event.getPlayer();
		if (! (player.isFlying() || mp.getStamina() <= WALLJUMP_COST) && (canWalljump(event.getTo()) || canWalljump(event.getFrom()))){
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
				
				// THIS is DO WALLJUMP
				// TODO refactor out actual doing walljump
				MovementPlayer mp = MovementPlusPlus.getMovementPlayer(player);
				player.setSprinting(true);
				player.setVelocity(player.getEyeLocation().getDirection().multiply(.75).setY(.75));
				mp.setStamina(mp.getStamina() - WALLJUMP_COST);
				
				// Polish
				Location walljumpLocation = player.getLocation();
				walljumpLocation.getWorld().playSound(walljumpLocation, Sound.BLOCK_METAL_STEP, 1.6f, .7f);
				walljumpLocation.getWorld().playSound(walljumpLocation, Sound.UI_TOAST_IN, 1.6f, 1.5f);
//				walljumpLocation.getWorld().playSound(walljumpLocation, Sound.AMBIENT_NETHER_WASTES_MOOD, 1.1f, 2f);
//				walljumpLocation.getWorld().playSound(walljumpLocation, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.1f, 1f);
				walljumpLocation.getWorld().spawnParticle(Particle.SWEEP_ATTACK, walljumpLocation, 1);
				
			}
			e.setCancelled(true);
			player.setAllowFlight(false);
			player.setFlying(false);
		}
	}
	
	public static boolean canWalljump(Location loc) {
		World world = loc.getWorld();
		double x = loc.getX();
		double y = Math.floor(loc.getY());
		double z = loc.getZ();
//		boolean northHalf = Math.floor(z) != Math.floor(z - 0.5);
//		boolean topHalf = Math.floor(y) != Math.floor(y + 0.5);
//		boolean eastHalf = Math.floor(x) != Math.floor(x + 0.5);
//		boolean northThird = Math.floor(z) != Math.floor(z - 0.66);
//		boolean southThird = Math.floor(z) != Math.floor(z + 0.66);
//		boolean topThird = Math.floor(y) != Math.floor(y + 0.66);
//		boolean bottomThird = Math.floor(y) != Math.floor(y - 0.66);
//		boolean eastThird = Math.floor(x) != Math.floor(x + 0.66);
//		boolean westThird = Math.floor(x) != Math.floor(x - 0.66);
		// ALL THESE FLOORS ARE NECESSARY - floor truncates towards negative infinity - prevents errors at 0
		Material south = world.getBlockAt ((int) Math.floor(x),     	(int) Math.floor(y),            (int) Math.floor((z + CB))) .getType();
		Material west = world.getBlockAt  ((int) Math.floor((x - CB)), 	(int) Math.floor(y),            (int) Math.floor(z))	    .getType();
		Material north = world.getBlockAt ((int) Math.floor(x),     	(int) Math.floor(y),            (int) Math.floor((z - CB))) .getType();
		Material east = world.getBlockAt  ((int) Math.floor((x + CB)), 	(int) Math.floor(y),            (int) Math.floor(z))	    .getType();
		Material south2 = world.getBlockAt((int) Math.floor(x),     	(int) Math.floor((y + .5)), 	(int) Math.floor((z + CB))) .getType();
		Material west2 = world.getBlockAt ((int) Math.floor((x - CB)), 	(int) Math.floor((y + .5)), 	(int) Math.floor(z))	    .getType();
		Material north2 = world.getBlockAt((int) Math.floor(x),     	(int) Math.floor((y + .5)), 	(int) Math.floor((z - CB))) .getType();
		Material east2 = world.getBlockAt ((int) Math.floor((x + CB)), 	(int) Math.floor((y + .5)), 	(int) Math.floor(z))	    .getType();
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
		Material southeast = world.getBlockAt ((int) Math.floor((x + OB)),	(int) Math.floor(y),            (int) Math.floor((z + OB))) .getType(); 
		Material southwest = world.getBlockAt ((int) Math.floor((x - OB)), 	(int) Math.floor(y),            (int) Math.floor((z + OB)))	.getType(); 
		Material northeast = world.getBlockAt ((int) Math.floor((x + OB)),  (int) Math.floor(y),            (int) Math.floor((z - OB))) .getType(); 
		Material northwest = world.getBlockAt ((int) Math.floor((x - OB)), 	(int) Math.floor(y),            (int) Math.floor((z - OB))) .getType(); 
		Material southeast2 = world.getBlockAt((int) Math.floor((x + OB)),	(int) Math.floor((y + .5)), 	(int) Math.floor((z + OB))) .getType(); 
		Material southwest2 = world.getBlockAt((int) Math.floor((x - OB)), 	(int) Math.floor((y + .5)), 	(int) Math.floor((z + OB))) .getType(); 
		Material northeast2 = world.getBlockAt((int) Math.floor((x + OB)),	(int) Math.floor((y + .5)), 	(int) Math.floor((z - OB))) .getType(); 
		Material northwest2 = world.getBlockAt((int) Math.floor((x - OB)), 	(int) Math.floor((y + .5)), 	(int) Math.floor((z - OB))) .getType();
		if (southeast.isSolid()	&& southeast != Material.BARRIER
				&& southeast2.isSolid() && southeast2 != Material.BARRIER){
				return true;
		} else if (southwest.isSolid()	&& southwest != Material.BARRIER
				&& southwest2.isSolid() && southwest2 != Material.BARRIER){
				return true;
		} else if (northeast.isSolid()	&& northeast != Material.BARRIER
				&& northeast2.isSolid() && northeast2 != Material.BARRIER){
				return true;
		} else if (northwest.isSolid()	&& northwest != Material.BARRIER
				&& northwest2.isSolid() && northwest2 != Material.BARRIER){
				return true;
		}
		
		return false;
	}
	
}
