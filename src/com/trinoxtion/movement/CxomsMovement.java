package com.trinoxtion.movement;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.UUID;
//
//import org.bukkit.Bukkit;
//import org.bukkit.entity.Player;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.EventPriority;
//import org.bukkit.event.Listener;
//import org.bukkit.event.entity.EntityDamageEvent;
//import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
//import org.bukkit.event.entity.EntityRegainHealthEvent;
//import org.bukkit.event.entity.FoodLevelChangeEvent;
//import org.bukkit.event.entity.PlayerDeathEvent;
//import org.bukkit.event.player.PlayerMoveEvent;
//import org.bukkit.event.player.PlayerQuitEvent;
//import org.bukkit.scheduler.BukkitRunnable;
//
//import com.trinoxtion.movement.jumping.Walljump;
//import com.trinoxtion.movement.pads.ElytraPad;
//import com.trinoxtion.movement.pads.HealthPad;
//import com.trinoxtion.movement.pads.JumpPad;
//import com.trinoxtion.movement.pads.Trampoline;
//
public final class CxomsMovement /*implements MovementSystem, Listener */{
//	
//	//Class<? extends MovementSystem> movementHandler();
//
//	private final BukkitRunnable stamregen = new BukkitRunnable(){ //This may cause future NPE problems, just a fore-prediction-note-warning
//		public void run(){
//			for(MovementPlayer mp : mPlayers.values())
//				mp.setStamina(mp.getStamina() + .5f);
//		}
//	};
//	
//	CxomsMovement(){
//		Bukkit.getServer().getPluginManager().registerEvents(new Walljump(), MovementPlusPlus.getPlugin());
//		Bukkit.getServer().getPluginManager().registerEvents(this, MovementPlusPlus.getPlugin());
//		Bukkit.getServer().getPluginManager().registerEvents(new ElytraPad(), MovementPlusPlus.getPlugin());
//		stamregen.runTaskTimer(MovementPlusPlus.getPlugin(), 12, 12);
//		HealthPad.getHealthPadTask(() -> mPlayers.values()).runTaskTimer(MovementPlusPlus.getPlugin(), 12, 12);
//		//TODO Jumppad & Trampoline Events ??
//	}
//	
//	public static Map<UUID, MovementPlayer> mPlayers = new HashMap<>();
//	
//	public void addPlayer(Player player){
//		mPlayers.put(player.getUniqueId(), new MovementPlayer(player));
//	}
//	
//	public void removePlayer(UUID uuid){
//		mPlayers.remove(uuid);
//	}
//	
//	public static boolean isMovementPlayer(Player player){ return isMovementPlayer(player.getUniqueId()); }
//	public static boolean isMovementPlayer(UUID uuid){
//		return mPlayers.get(uuid) != null;
//	}
//	
//	public static MovementPlayer getMovementPlayer(Player player){ return getMovementPlayer(player.getUniqueId()); }
//	public static MovementPlayer getMovementPlayer(UUID uuid){
//		return mPlayers.get(uuid);
//	}
//	
//	//TODO OVERALL: TEST PADS, LAUNCHERS, FINISH GLIDE 
//	
//	@EventHandler
//	public static void onMovement(PlayerMoveEvent e){
//		if (isMovementPlayer(e.getPlayer())){
//			MovementPlayer mp = getMovementPlayer(e.getPlayer());
//			if(!mp.isJumping()){
//				if (e.getTo().getY() > e.getFrom().getY())
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
//		if (e.getEntity() instanceof Player && isMovementPlayer((Player) e.getEntity()) && e.getCause() == DamageCause.FALL){
//			e.setCancelled(true);
//		}
//	}
//	
//	@EventHandler
//	public static void onFoodLevelChange(FoodLevelChangeEvent e) {
//		if (e.getEntity() instanceof Player && isMovementPlayer((Player) e.getEntity())){
//			e.setCancelled(true);
//		}
//	}
//	
//	@EventHandler
//	public static void onPlayerRegainHealth(EntityRegainHealthEvent e) {
//		if (e.getEntity() instanceof Player && isMovementPlayer((Player) e.getEntity())){
//			e.setCancelled(true);
//		}
//	}
//	
//	@EventHandler
//	public static void onDeathEvent(PlayerDeathEvent e){
//		if (e.getEntity() instanceof Player){
//			Player player = (Player) e.getEntity();
//			if (isMovementPlayer(player)){
//				getMovementPlayer(player).setStamina(MovementPlayer.FULL_STAMINA);
//			}
//		}
//	}
//	
//	@EventHandler
//	public void onPlayerLeave(PlayerQuitEvent e){
//		Player player = e.getPlayer();
//		//VarInt eid = player.getMetadata(arg0)
//		if (isMovementPlayer(player)){
//			removePlayer(e.getPlayer());
//		}
//	}
//	
}
