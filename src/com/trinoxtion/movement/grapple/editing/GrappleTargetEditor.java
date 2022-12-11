package com.trinoxtion.movement.grapple.editing;

import com.trinoxtion.movement.MovementPlusPlus;
import com.trinoxtion.movement.editing.WandBasedEditor;
import com.trinoxtion.movement.grapple.GrappleTarget;
import com.trinoxtion.movement.grapple.GrappleTargetManager;
import com.trinoxtion.movement.grapple.TargetGrappling;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.punchtree.util.particle.ParticleShapes;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.lang.annotation.Target;
import java.util.Optional;
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
    private static double MOVE_STEP_SIZE = 0.5;


    private final GrappleTargetManager grappleTargetManager;

    public GrappleTargetEditor(GrappleTargetManager grappleTargetManager) {
        this.grappleTargetManager = grappleTargetManager;
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
//        editor.sendMessage("Debug: Right click with a grapple wand!");
        editor.sendMessage("Debug: There are " + getCollectionTargets().size() + " collection target(s)");
        Vector start = editor.getEyeLocation().toVector();
        Vector end = start.clone().add(editor.getLocation().getDirection().multiply(REACH));
//        ParticleShapes.spawnParticleLine(start.toLocation(editor.getWorld()), end.toLocation(editor.getWorld()), (int) (REACH * 5));

        Optional<GrappleTarget> potentialClickedTarget = getCollectionTargets().stream()
                .peek(target -> {
                    if (TargetGrappling.lineCrossesTargetPlane(target, start, end)) {
                        editor.sendMessage("Debug: crosses target plane " + String.format("(%.02f %.02f %.02f)!", target.location().getX(), target.location().getY(), target.location().getZ()));
                    }
                    ParticleShapes.spawnParticleLine(target.location(), target.location().clone().add(target.facingDirection().clone().multiply(2)), 10);
                })
                .filter(target -> didHit(target, start, end))
                .min((a, b) -> (int) Math.signum(a.location().distance(editor.getLocation()) - b.location().distance(editor.getLocation())));

        if (potentialClickedTarget.isEmpty()) return;

        GrappleTarget clickedTarget = potentialClickedTarget.get();
        editor.sendMessage(ChatColor.GREEN + "Hit target!");

        GrappleTargetEditingMode mode = GrappleTargetEditingMode.getModeFromItem(wand);

        editor.sendMessage(ChatColor.GREEN + "Editing mode is " + mode.name());

        ArmorStand armorStand = clickedTarget.getArmorStand();

        if (armorStand == null) {
            editor.sendMessage(ChatColor.RED + "Armor stand is null!");
            return;
        }

        switch (mode) {
            case MOVING_X -> {
                editor.sendMessage(ChatColor.GREEN + "Increasing target x!");
                clickedTarget.moveTo(armorStand.getLocation().add(MOVE_STEP_SIZE, 0, 0));
            }
            case MOVING_Y -> {
                editor.sendMessage(ChatColor.GREEN + "Increasing target y!");
                clickedTarget.moveTo(armorStand.getLocation().add(0, MOVE_STEP_SIZE, 0));
            }
            case MOVING_Z -> {
                editor.sendMessage(ChatColor.GREEN + "Increasing target z!");
                clickedTarget.moveTo(armorStand.getLocation().add(0, 0, MOVE_STEP_SIZE));
            }
        }
    }

    private Set<GrappleTarget> getCollectionTargets() {
        // TODO implement collections
        return grappleTargetManager.getTemporaryGlobalCollection();
    }

    private boolean didHit(GrappleTarget target, Vector a, Vector b) {
        if (!TargetGrappling.lineCrossesTargetPlane(target, a, b)) {
            return false;
        }
        Vector intersection = TargetGrappling.getGrappleTargetPlaneIntersection(target, a, b);
        double intersectionDistance = intersection.distance(target.location().toVector());
        return intersectionDistance < target.radius();
    }

    @Override
    public void onLeftClick(Player editor, ItemStack wand) {
        editor.sendMessage("Debug: Left click with a grapple wand!");
        new GrappleTargetEditingOptionsMenu(wand).showOptionsMenu(editor);
    }

    @Override
    public void onMenuClick(Player editor, ItemStack wand, InventoryClickEvent inventoryClickEvent) {
        if (isGrappleEditingOptionsMenu(inventoryClickEvent)) {
            editor.sendMessage("Debug: Menu click in a grapple menu!");
            inventoryClickEvent.setCancelled(true);

            if (inventoryClickEvent.getCurrentItem() == null) return;
            GrappleTargetEditingMode mode = GrappleTargetEditingMode.getModeFromItem(inventoryClickEvent.getCurrentItem());
            editor.sendMessage(ChatColor.GREEN + "Set mode to " + mode.name());
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
        CHANGING_COLLECTION;

        public static GrappleTargetEditingMode getModeFromItem(ItemStack itemStack) {
            assert itemStack.hasItemMeta();
            NamespacedKey modeKey = new NamespacedKey(MovementPlusPlus.getPlugin(), "grapple_target.editing_mode");
            PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
            if (container.has(modeKey, PersistentDataType.STRING)) {
                return GrappleTargetEditingMode.valueOf(container.get(modeKey, PersistentDataType.STRING));
            } else {
                return MOVING_X;
            }
        }
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
