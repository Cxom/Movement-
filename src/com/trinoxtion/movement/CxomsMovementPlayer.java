package com.trinoxtion.movement;
//
//import org.bukkit.entity.Player;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.EventPriority;
//import org.bukkit.event.Listener;
//import org.bukkit.event.entity.EntityDamageEvent;
//import org.bukkit.event.entity.EntityRegainHealthEvent;
//import org.bukkit.event.entity.FoodLevelChangeEvent;
//import org.bukkit.event.entity.PlayerDeathEvent;
//import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
//import org.bukkit.event.player.PlayerMoveEvent;
//import org.bukkit.event.player.PlayerQuitEvent;
//import org.bukkit.scheduler.BukkitRunnable;
//
//import com.trinoxtion.movement.pads.ElytraPad;
//import com.trinoxtion.movement.pads.JumpPad;
//import com.trinoxtion.movement.pads.Trampoline;
//
public class CxomsMovementPlayer /* extends MovementPlayer implements Listener */ {
//
//	private final BukkitRunnable stamregen = new BukkitRunnable(){ //This may cause future NPE problems, just a fore-prediction-note-warning
//		public void run(){
//				setStamina(getStamina() + .5f);
//		}
//	};	
//	
//	CxomsMovementPlayer(Player player) {
//		super(player);
//		
//	}
//	
//	@Override
//	public void onMovement(MovementPlayer mp, PlayerMoveEvent e){
//			if(!mp.isJumping()){
//				if (e.getTo().getY() > e.getFrom().getY()){
//					JumpPad.jumppad(mp);
//				//boolean launcher = Launcher.launcher(mp);
//				/*if (!launcher)*/ 
//					Trampoline.trampoline(mp);
//					ElytraPad.elytraPad(mp); //TODO setJumping() - Boolean based.
//			}
//		}
//	}
//	
//	@EventHandler(priority = EventPriority.HIGHEST)
//	public static void onFallDamage(EntityDamageEvent e){
//		if (e.getEntity() instanceof Player 
//				&& e.getCause() == DamageCause.FALL
//				&& MovementPlusPlus.isMovementPlayer((Player) e.getEntity())){
//			e.setCancelled(true);
//		}
//	}
//	
//	@EventHandler
//	public static void onFoodLevelChange(FoodLevelChangeEvent e) {
//		if (e.getEntity() instanceof Player && MovementPlusPlus.isMovementPlayer((Player) e.getEntity())){
//			e.setCancelled(true);
//		}
//	}
//	
//	@EventHandler
//	public static void onPlayerRegainHealth(EntityRegainHealthEvent e) {
//		if (e.getEntity() instanceof Player && MovementPlusPlus.isMovementPlayer((Player) e.getEntity())){
//			e.setCancelled(true);
//		}
//	}
//	
//	@EventHandler
//	public static void onDeathEvent(PlayerDeathEvent e){
//		if (e.getEntity() instanceof Player){
//			Player player = (Player) e.getEntity();
//			if (MovementPlusPlus.isMovementPlayer(player)){
//				MovementPlusPlus.getMovementPlayer(player).setStamina(MovementPlayer.FULL_STAMINA);
//			}
//		}
//	}
//	
//	@EventHandler
//	public static void onPlayerLeave(PlayerQuitEvent e){
//		Player player = e.getPlayer();
//		if (MovementPlusPlus.isMovementPlayer(player)){
//			MovementPlusPlus.deregisterPlayer(e.getPlayer());
//		}
//	}
//
}
