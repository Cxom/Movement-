package com.trinoxtion.movement.grapple.editing;

import com.trinoxtion.movement.editing.WandBasedEditor;
import com.trinoxtion.movement.grapple.GrappleTarget;
import com.trinoxtion.movement.grapple.TargetGrappling;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class GrappleTargetEditor implements WandBasedEditor {

    private static final double REACH = 7;
    private static ItemStack BASE_WAND = new ItemStack(Material.BLAZE_ROD);
    static {
        BASE_WAND.editMeta(meta -> meta.displayName(Component
                .text("Grapple Target Editor Wand")
                .color(GrappleTarget.DEFAULT_COLOR)
                .decoration(TextDecoration.ITALIC, false)));
    }

    public static ItemStack getNewWand() {
        return BASE_WAND.clone();
    }

    @Override
    public boolean isWand(ItemStack itemStack) {
        return itemStack != null &&
                itemStack.getType() == BASE_WAND.getType() &&
                itemStack.hasItemMeta() &&
                itemStack.getItemMeta().displayName().equals(BASE_WAND.getItemMeta().displayName());
    }

    @Override
    public void onRightClick(Player editor, ItemStack wand) {
        editor.sendMessage("Debug: Right click with a grapple wand!");
        Vector start = editor.getEyeLocation().toVector();
        Vector end = start.clone().add(editor.getLocation().getDirection().multiply(REACH));
        for (GrappleTarget target : getCollectionTargets()) {
            if (didHit(target, start, end)) {
                editor.sendMessage(ChatColor.GREEN + "Updated target!");
                return;
            }
        }
    }

    private Set<GrappleTarget> getCollectionTargets() {
        // TODO implement collections
        // TODO return one global collection for starters
        return new HashSet<>();
    }

    private boolean didHit(GrappleTarget target, Vector a, Vector b) {
        if (!TargetGrappling.lineCrossesTargetPlane(target, a, b)) {
            return false;
        }
        Vector rayDirection = b.clone().subtract(a);
        Vector rayEndToTarget = target.location().toVector().subtract(b);
        double t = rayEndToTarget.dot(target.facingDirection()) / rayDirection.dot(target.facingDirection());
        Vector intersection = a.clone().add(rayDirection.multiply(t));
        double intersectionDistance = intersection.distance(target.location().toVector());
        return intersectionDistance < target.radius();
    }

    @Override
    public void onLeftClick(Player editor, ItemStack wand) {
        editor.sendMessage("Debug: Left click with a grapple wand!");
        // TODO extract data from wand lore
        new GrappleTargetEditingOptionsMenu(wand).showOptionsMenu(editor);
    }

    @Override
    public void onMenuClick(Player editor, ItemStack wand, InventoryClickEvent inventoryClickEvent) {
        if (isGrappleEditingOptionsMenu(inventoryClickEvent)) {
            editor.sendMessage("Debug: Menu click in a grapple menu!");
            inventoryClickEvent.setCancelled(true);
        }
    }

    private boolean isGrappleEditingOptionsMenu(InventoryClickEvent inventoryClickEvent) {
        return inventoryClickEvent.getView().title().equals(GrappleTargetEditingOptionsMenu.MENU_TITLE) &&
                inventoryClickEvent.getClickedInventory() != null && // null if clicked outside both invs
                inventoryClickEvent.getInventory().getSize() == 9;
    }

    private enum GrappleTargetEditingMode {
        MOVING_X,
        MOVING_Y,
        MOVING_Z,
        ROTATING_BY_HEAD,
        ROTATING_BY_PRESET,
        DELETING,
        CHANGING_COLLECTION
    }

    private static class GrappleTargetEditingOptionsMenu {

        private static final Component MENU_TITLE = Component
                .text("Grapple Target Editing Options")
                .color(GrappleTarget.DEFAULT_COLOR)
                .decoration(TextDecoration.ITALIC, false);

        private static final ItemStack MOVING_X_ITEM = new ItemStack(Material.RED_WOOL);
        private static final ItemStack MOVING_Y_ITEM = new ItemStack(Material.GREEN_WOOL);
        private static final ItemStack MOVING_Z_ITEM = new ItemStack(Material.BLUE_WOOL);
        private static final ItemStack ROTATING_BY_HEAD_ITEM = new ItemStack(Material.ENDER_EYE);
        private static final ItemStack ROTATING_BY_PRESET_ITEM = new ItemStack(Material.ARROW);
        private static final ItemStack DELETING_ITEM = new ItemStack(Material.BARRIER);
        private static final ItemStack CHANGING_COLLECTION_ITEM = new ItemStack(Material.ANVIL);

        static {
            MOVING_X_ITEM.editMeta(meta -> meta.displayName(Component.text("Move X").decoration(TextDecoration.ITALIC, false)));
            MOVING_Y_ITEM.editMeta(meta -> meta.displayName(Component.text("Move Y").decoration(TextDecoration.ITALIC, false)));
            MOVING_Z_ITEM.editMeta(meta -> meta.displayName(Component.text("Move Z").decoration(TextDecoration.ITALIC, false)));
            ROTATING_BY_HEAD_ITEM.editMeta(meta -> meta.displayName(Component.text("Rotate by head").decoration(TextDecoration.ITALIC, false)));
            ROTATING_BY_PRESET_ITEM.editMeta(meta -> meta.displayName(Component.text("Rotate by preset").decoration(TextDecoration.ITALIC, false)));
            DELETING_ITEM.editMeta(meta -> meta.displayName(Component.text("Delete").decoration(TextDecoration.ITALIC, false)));
            CHANGING_COLLECTION_ITEM.editMeta(meta -> meta.displayName(Component.text("Change collection").decoration(TextDecoration.ITALIC, false)));
        }

        private Inventory menu;

        private GrappleTargetEditingOptionsMenu(ItemStack wand) {
            menu = Bukkit.createInventory(null, 9, MENU_TITLE);
            menu.setItem(0, MOVING_X_ITEM);
            menu.setItem(1, MOVING_Y_ITEM);
            menu.setItem(2, MOVING_Z_ITEM);
            menu.setItem(4, ROTATING_BY_HEAD_ITEM);
            menu.setItem(5, ROTATING_BY_PRESET_ITEM);
            menu.setItem(7, CHANGING_COLLECTION_ITEM);
            menu.setItem(8, DELETING_ITEM);
        }

        private void showOptionsMenu(Player editor) {
            editor.openInventory(menu);
        }
    }

}
