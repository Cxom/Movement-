package com.trinoxtion.movement.editing;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;

public interface WandBasedEditor {

    boolean isWand(ItemStack itemStack);

    default void onRightClick(Player editor, ItemStack wand) {}

    default void onLeftClick(Player editor, ItemStack wand) {}

    default void onMenuClick(Player editor, ItemStack wand, InventoryClickEvent inventoryClickEvent) {}

    default void onMenuDrag(Player editor, ItemStack wand, InventoryDragEvent event) {}
}
