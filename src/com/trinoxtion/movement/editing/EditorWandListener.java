package com.trinoxtion.movement.editing;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class EditorWandListener implements Listener {

    private final WandBasedEditor wandBasedEditor;

    public EditorWandListener(WandBasedEditor wandBasedEditor) {
        this.wandBasedEditor = wandBasedEditor;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!wandBasedEditor.isWand(event.getItem())) return;

        ItemStack wand = event.getItem();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            wandBasedEditor.onRightClick(player, wand);
        } else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            wandBasedEditor.onLeftClick(player, wand);
        }
    }

    @EventHandler
    public void onPlayerMenuClick(InventoryClickEvent event) {
        onPlayerMenuInteract(event);
    }

    // Abstract for supporting drag listening turned out unnecessary if you're just cancelling click event anyway
    // You can't drag without clicking
    @EventHandler
    public void onPlayerMenuDrag(InventoryDragEvent event) {
        onPlayerMenuInteract(event);
    }

    private void onPlayerMenuInteract(InventoryInteractEvent event) {
        if ( ! (event.getWhoClicked() instanceof Player player)) return;

        ItemStack wand;
        if (wandBasedEditor.isWand(player.getInventory().getItemInMainHand())) {
            wand = player.getInventory().getItemInMainHand();
        } else if (wandBasedEditor.isWand(player.getInventory().getItemInOffHand())) {
            wand = player.getInventory().getItemInOffHand();
        } else {
            return;
        }

        if (event instanceof InventoryClickEvent inventoryClickEvent) {
            wandBasedEditor.onMenuClick(player, wand, inventoryClickEvent);
        } else if (event instanceof InventoryDragEvent inventoryDragEvent) {
            wandBasedEditor.onMenuDrag(player, wand, inventoryDragEvent);
        }
    }

}
