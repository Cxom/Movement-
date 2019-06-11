package com.trinoxtion.movement.pads;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import com.trinoxtion.movement.MovementComponent;

public abstract class SimplePad implements MovementComponent {

	protected final Material material;
	
	public SimplePad(Material material) {
		this.material = material;
	}

	public Material getMaterial() {
		return material;
	}
	
	public boolean accept(Location l) {
		Block b = l.clone().subtract(0, 1, 0).getBlock();
		return b.getType() == getMaterial();
	}
	
}
