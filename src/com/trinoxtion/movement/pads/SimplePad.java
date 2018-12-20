package com.trinoxtion.movement.pads;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import com.trinoxtion.movement.MovementComponent;

public abstract class SimplePad implements MovementComponent {

	protected final Material material;
	protected final byte data;

	public SimplePad(Material material) {
		this(material, (byte) -1);
	}
	
	public SimplePad(Material material, byte data) {
		this.material = material;
		this.data = data;
	}

	public Material getMaterial() {
		return material;
	}
	
	public byte getData(){
		return data;
	}
	
	@SuppressWarnings("deprecation")
	public boolean accept(Location l) {
		Block b = l.clone().subtract(0, 1, 0).getBlock();
		return b.getType() == getMaterial() && (data == -1 || data == b.getData());
	}
	
}
