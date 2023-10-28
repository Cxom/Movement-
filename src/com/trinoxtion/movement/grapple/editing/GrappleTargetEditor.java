package com.trinoxtion.movement.grapple.editing;

import com.trinoxtion.movement.MovementPlusPlus;
import com.trinoxtion.movement.editing.WandBasedEditor;
import com.trinoxtion.movement.grapple.GrappleFacingDirection;
import com.trinoxtion.movement.grapple.GrappleTarget;
import com.trinoxtion.movement.grapple.GrappleTargetManager;
import com.trinoxtion.movement.grapple.TargetGrappling;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.Optional;
import java.util.Set;

public class GrappleTargetEditor implements WandBasedEditor {

    private static final double REACH = 7;
    private static final String BASE_WAND_NAME = "Grapple Target Editor Wand";
    private static final Component BASE_WAND_NAME_COMPONENT = Component
            .text(BASE_WAND_NAME)
            .color(GrappleTarget.DEFAULT_COLOR)
            .decoration(TextDecoration.ITALIC, false);
    private static final ItemStack BASE_WAND = new ItemStack(Material.BLAZE_ROD);
    static {
        BASE_WAND.editMeta(meta -> meta.displayName(BASE_WAND_NAME_COMPONENT));
    }
    private static final double MOVE_STEP_SIZE = 0.5;
    private static final NamespacedKey EDITING_MODE_NAMESPACED_KEY = new NamespacedKey(MovementPlusPlus.getPlugin(), "grapple_target.editing_mode");



    private final GrappleTargetManager grappleTargetManager;

    public GrappleTargetEditor(GrappleTargetManager grappleTargetManager) {
        this.grappleTargetManager = grappleTargetManager;
    }

    public static ItemStack getNewWand() {
        return BASE_WAND.clone();
    }

    @Override
    public boolean isWand(ItemStack itemStack) {
        if (itemStack == null ||
                itemStack.getType() != BASE_WAND.getType() ||
                !itemStack.hasItemMeta()) {
            return false;
        }
        TextComponent wandName = (TextComponent) itemStack.getItemMeta().displayName();
        return wandName != null && wandName.content().startsWith(BASE_WAND_NAME);
    }

    private record GrappleTargetRaycastHitQuery(boolean didHitTarget, GrappleTarget clickedTarget, ArmorStand armorStand) {}

    @Override
    public void onRightClick(Player editor, ItemStack wand) {
//        editor.sendMessage("Debug: Right click with a grapple wand!");
//        editor.sendMessage("Debug: There are " + getCollectionTargets().size() + " collection target(s)");
        GrappleTargetRaycastHitQuery query = raycastToHitGrappleTarget(editor);

        if (!query.didHitTarget()) {
            return;
        }

        GrappleTargetEditingMode mode = GrappleTargetEditingMode.getModeFromItem(wand);
        if (mode == null) {
            editor.sendMessage(ChatColor.RED + "Select an editing mode by left clicking first!");
            return;
        }

        switch (mode) {
            case MOVING_X -> {
                query.clickedTarget().moveTo(query.armorStand().getLocation().add(MOVE_STEP_SIZE, 0, 0));
            }
            case MOVING_Y -> {
                query.clickedTarget().moveTo(query.armorStand().getLocation().add(0, MOVE_STEP_SIZE, 0));
            }
            case MOVING_Z -> {
                query.clickedTarget().moveTo(query.armorStand().getLocation().add(0, 0, MOVE_STEP_SIZE));
            }
            case ROTATING_TO_FACE_YOU -> {
                rotateTargetByHead(editor, query.clickedTarget());
            }
            case CHANGING_COLLECTION -> {
                changeTargetCollection(wand, query.clickedTarget());
            }
            case DELETING -> {
                deleteTarget(editor, query.clickedTarget());
            }
        }
    }

    @Override
    public void onLeftClick(Player editor, ItemStack wand) {
//        editor.sendMessage("Debug: Left click with a grapple wand!");
        GrappleTargetRaycastHitQuery query = raycastToHitGrappleTarget(editor);

        if (!query.didHitTarget()) {
            new GrappleTargetEditingOptionsMenu(wand).showOptionsMenu(editor);
            return;
        }

        GrappleTargetEditingMode mode = GrappleTargetEditingMode.getModeFromItem(wand);
        if (mode == null) {
            editor.sendMessage(ChatColor.RED + "Select an editing mode by left clicking first!");
            return;
        }

        switch (mode) {
            case MOVING_X -> {
                query.clickedTarget().moveTo(query.armorStand().getLocation().subtract(MOVE_STEP_SIZE, 0, 0));
            }
            case MOVING_Y -> {
                query.clickedTarget().moveTo(query.armorStand().getLocation().subtract(0, MOVE_STEP_SIZE, 0));
            }
            case MOVING_Z -> {
                query.clickedTarget().moveTo(query.armorStand().getLocation().subtract(0, 0, MOVE_STEP_SIZE));
            }
            case ROTATING_TO_FACE_YOU -> {
                rotateTargetByHead(editor, query.clickedTarget());
            }
            case CHANGING_COLLECTION -> {
                changeTargetCollection(wand, query.clickedTarget());
            }
            case DELETING -> {
                deleteTarget(editor, query.clickedTarget());
            }
            // Non-moving modes have no left click behavior
        }
    }

    private void rotateTargetByHead(Player editor, GrappleTarget clickedTarget) {
        clickedTarget.rotateTo(GrappleFacingDirection.getNearestFacingDirection(editor.getLocation().getDirection()));
    }

    private void changeTargetCollection(ItemStack wand, GrappleTarget clickedTarget) {
        // TODO
    }

    private void deleteTarget(Player editor, GrappleTarget grappleTarget) {
        grappleTargetManager.deleteTarget(grappleTarget);
        editor.sendMessage(ChatColor.RED + "Deleted target");
        editor.playSound(editor.getLocation(), Sound.BLOCK_BAMBOO_WOOD_BREAK, 1, 1);
    }


    private GrappleTargetRaycastHitQuery raycastToHitGrappleTarget(Player editor) {
        Vector start = editor.getEyeLocation().toVector();
        Vector end = start.clone().add(editor.getLocation().getDirection().multiply(REACH));
//        ParticleShapes.spawnParticleLine(start.toLocation(editor.getWorld()), end.toLocation(editor.getWorld()), (int) (REACH * 5));

        Optional<GrappleTarget> potentialClickedTarget = getCollectionTargets().stream()
//                .peek(target -> {
//                    if (TargetGrappling.lineCrossesTargetPlane(target, start, end)) {
////                        editor.sendMessage("Debug: crosses target plane " + String.format("(%.02f %.02f %.02f)!", target.location().getX(), target.location().getY(), target.location().getZ()));
//                    }
//                    ParticleShapes.spawnParticleLine(target.location(), target.location().clone().add(target.facingDirection().clone().multiply(2)), 10);
//                })
                .filter(target -> didHit(target, start, end) || didHit(target, end, start))
                .min((a, b) -> (int) Math.signum(a.location().distance(editor.getLocation()) - b.location().distance(editor.getLocation())));

        if (potentialClickedTarget.isEmpty()) {
            return new GrappleTargetRaycastHitQuery(false, null, null);
        }

        GrappleTarget clickedTarget = potentialClickedTarget.get();

        ArmorStand armorStand = clickedTarget.getArmorStand();

        if (armorStand == null) {
            editor.sendMessage(ChatColor.RED + "Armor stand is null!");
            throw new NullPointerException("Armor stand is null!");
        }

        return new GrappleTargetRaycastHitQuery(true, clickedTarget, armorStand);
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
    public void onMenuClick(Player editor, ItemStack wand, InventoryClickEvent inventoryClickEvent) {
        if (isGrappleEditingOptionsMenu(inventoryClickEvent)) {
            editor.sendMessage("Debug: Menu click in a grapple menu!");
            inventoryClickEvent.setCancelled(true);

            if (inventoryClickEvent.getCurrentItem() == null) return;
            // TODO rather than close, make the item glow and other one not glow
            inventoryClickEvent.getView().close();

            GrappleTargetEditingMode mode = GrappleTargetEditingMode.getModeFromItem(inventoryClickEvent.getCurrentItem());
            setMode(editor, wand, mode);
        }
    }

    private void setMode(Player editor, ItemStack wand, GrappleTargetEditingMode mode) {
        editor.sendMessage(ChatColor.GREEN + "Set mode to " + mode.name());
        wand.editMeta(meta -> {
            meta.displayName(
                    BASE_WAND_NAME_COMPONENT
                            .append(Component.text(" - ").color(NamedTextColor.GRAY))
                            .append(Component.text(mode.name()).color(NamedTextColor.RED)));
            meta.getPersistentDataContainer().set(EDITING_MODE_NAMESPACED_KEY, PersistentDataType.STRING, mode.name());
        });
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
        ROTATING_TO_FACE_YOU,
        DELETING,
        CHANGING_COLLECTION;

        public static GrappleTargetEditingMode getModeFromItem(ItemStack itemStack) {
            assert itemStack.hasItemMeta();
            PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
            if (container.has(EDITING_MODE_NAMESPACED_KEY, PersistentDataType.STRING)) {
                return GrappleTargetEditingMode.valueOf(container.get(EDITING_MODE_NAMESPACED_KEY, PersistentDataType.STRING));
            } else {
                return null;
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
        private static final ItemStack ROTATING_TO_FACE_YOU_ITEM = new ItemStack(Material.ENDER_EYE);
        private static final ItemStack DELETING_ITEM = new ItemStack(Material.BARRIER);
        private static final ItemStack CHANGING_COLLECTION_ITEM = new ItemStack(Material.ANVIL);

        static {
            MOVING_X_ITEM.editMeta(meta -> {
                meta.displayName(Component.text("Move X").decoration(TextDecoration.ITALIC, false));
                setPersistentDataMode(meta, GrappleTargetEditingMode.MOVING_X.name());
            });
            MOVING_Y_ITEM.editMeta(meta -> {
                meta.displayName(Component.text("Move Y").decoration(TextDecoration.ITALIC, false));
                setPersistentDataMode(meta, GrappleTargetEditingMode.MOVING_Y.name());
            });
            MOVING_Z_ITEM.editMeta(meta -> {
                meta.displayName(Component.text("Move Z").decoration(TextDecoration.ITALIC, false));
                setPersistentDataMode(meta, GrappleTargetEditingMode.MOVING_Z.name());
            });
            ROTATING_TO_FACE_YOU_ITEM.editMeta(meta -> {
                meta.displayName(Component.text("Rotate to face you").decoration(TextDecoration.ITALIC, false));
                setPersistentDataMode(meta, GrappleTargetEditingMode.ROTATING_TO_FACE_YOU.name());
            });
            DELETING_ITEM.editMeta(meta -> {
                meta.displayName(Component.text("Delete").decoration(TextDecoration.ITALIC, false));
                setPersistentDataMode(meta, GrappleTargetEditingMode.DELETING.name());
            });
            CHANGING_COLLECTION_ITEM.editMeta(meta -> {
                meta.displayName(Component.text("Change collection").decoration(TextDecoration.ITALIC, false));
                setPersistentDataMode(meta, GrappleTargetEditingMode.CHANGING_COLLECTION.name());
            });
        }

        private static void setPersistentDataMode(ItemMeta meta, String name) {
            meta.getPersistentDataContainer().set(EDITING_MODE_NAMESPACED_KEY, PersistentDataType.STRING, name);
        }

        private Inventory menu;

        private GrappleTargetEditingOptionsMenu(ItemStack wand) {
            menu = Bukkit.createInventory(null, 9, MENU_TITLE);
            menu.setItem(0, MOVING_X_ITEM);
            menu.setItem(1, MOVING_Y_ITEM);
            menu.setItem(2, MOVING_Z_ITEM);
            menu.setItem(4, ROTATING_TO_FACE_YOU_ITEM);
            menu.setItem(6, CHANGING_COLLECTION_ITEM);
            menu.setItem(8, DELETING_ITEM);
        }

        private void showOptionsMenu(Player editor) {
            editor.openInventory(menu);
        }
    }

}
