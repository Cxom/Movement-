package com.trinoxtion.movement.launchers;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.Vector;

import com.trinoxtion.movement.MovementComponent;
import com.trinoxtion.movement.MovementPlayer;

public class Launchers implements MovementComponent {

	public static final Launchers DEFAULT = new Launchers(5, new Material[] {
																Material.DIAMOND_BLOCK });
	
	private static final String LAUNCHER_DESIGNATION_STRING = "[Launcher]";
	
	private final boolean launcherAdd = false;
	
	private final int searchDepth;
	private final Material[] blockMask;
	
	public Launchers(int searchDepth, Material[] blockMask) {
		this.searchDepth = searchDepth;
		this.blockMask = blockMask;
	}
	
	@Override
	public void onMovement(PlayerMoveEvent event, MovementPlayer mp) {
		Location l = event.getTo();
		Launcher launcher = getLauncher(l);
		if (launcher != null
				&& !mp.isJumping()
				&& event.getPlayer().isOnGround()
				&& event.getTo().getY() <= event.getFrom().getY()) {
			launcher.launch(mp);
		}
	}
	
	public Launcher getLauncher(Location l) {
		if (inBlockMask(l)) {
			return getLauncherFromSignBeneath(l);
		}
		return null;
	}
	
	public boolean inBlockMask(Location l) {
		Block b = l.clone().subtract(0, 1, 0).getBlock();
		boolean accept = false;
		for (Material material : blockMask) {
			accept |= (b.getType() == material);
		}
		return accept;
	}
	
	public Launcher getLauncherFromSignBeneath(Location l) {
		Sign launcherSign;
		
		for (int i = 0; i < searchDepth; ++i) {
			Block b = l.clone().subtract(0, 2+i, 0).getBlock();
			launcherSign = getLauncherSignFromBlock(b);
			if (launcherSign != null) {
				return getLauncherFromSign(launcherSign);
			}
		}
		return null;
	}
	
	private Sign getLauncherSignFromBlock(Block block) {
		if (isSign(block.getType())) {
			Sign launcherSign = (Sign) block.getState();
			if (hasLauncherDesignation(launcherSign)) {
				return launcherSign;
			}
		}
		return null;
	}
	
	private Launcher getLauncherFromSign(Sign launcherSign) {
		String yawString = launcherSign.getLine(1);
		String pitchString = launcherSign.getLine(2);
		String powerString = launcherSign.getLine(3);
		
		Launcher launcher;
		try {
			Double yaw = Math.toRadians(Double.parseDouble(yawString));
			Double pitch = -1 * Math.toRadians(Double.parseDouble(pitchString));
			Double power = Double.parseDouble(powerString);
			Double x = Math.cos(pitch) * Math.sin(-yaw);
			Double y = Math.sin(pitch);
			Double z = Math.cos(pitch) * Math.cos(yaw);
			Vector launchVector = new Vector(x, y, z).normalize();
			launcher = new Launcher(launchVector, power, launcherAdd);
		} catch(NumberFormatException e) {
			Bukkit.getLogger().warning("Couldn't parse launcher from sign - " + e.getMessage());
			launcher = null;
		}
		return launcher;
	}
	
	private boolean hasLauncherDesignation(Sign sign) {
		return LAUNCHER_DESIGNATION_STRING.equals(sign.getLine(0));
	}
	
	final List<Material> SIGN_MATERIALS = Arrays.asList(
		Material.ACACIA_SIGN,
		Material.ACACIA_WALL_SIGN,
		Material.BIRCH_SIGN,
		Material.BIRCH_WALL_SIGN,
		Material.DARK_OAK_SIGN,
		Material.DARK_OAK_WALL_SIGN,
		Material.JUNGLE_SIGN,
		Material.JUNGLE_WALL_SIGN,
		Material.OAK_SIGN,
		Material.OAK_WALL_SIGN,
		Material.SPRUCE_SIGN,
		Material.SPRUCE_WALL_SIGN
	);
	private boolean isSign(Material material) {
		return SIGN_MATERIALS.contains(material);
	}
	
}
