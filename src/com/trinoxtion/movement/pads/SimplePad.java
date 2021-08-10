package com.trinoxtion.movement.pads;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import com.trinoxtion.movement.MovementComponent;

public abstract class SimplePad implements MovementComponent {

	public static final double BLOCK_BELOW_DETECTION_RANGE = 0.1;
	
	protected final Material material;
	
	public SimplePad(Material material) {
		this.material = material;
	}

	public Material getMaterial() {
		return material;
	}
	
	public boolean accept(Location l) {
		Block b = l.clone().subtract(0, BLOCK_BELOW_DETECTION_RANGE, 0).getBlock();
		return b.getType() == getMaterial();
	}
	
}
