package dev.ntgame.overenchant.listeners;

import dev.ntgame.overenchant.OverEnchantPlugin;
import dev.ntgame.overenchant.config.OverEnchantConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Vanilla's own anvil combine rule already turns "same enchant, same level" into
 * level+1 (V + V = VI) - it just refuses to go past {@link Enchantment#getMaxLevel()}.
 * This only steps in for the specific case where that vanilla cap is what's blocking
 * the merge, and replaces the (otherwise empty/vanilla) result with one that carries
 * the enchantment up to this plugin's configured cap instead.
 */
public class AnvilOverenchantListener implements Listener {

    private final OverEnchantPlugin plugin;

    public AnvilOverenchantListener(OverEnchantPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        OverEnchantConfig config = plugin.getOverEnchantConfig();
        ItemStack base = event.getInventory().getFirstItem();
        ItemStack addition = event.getInventory().getSecondItem();
        if (base == null || addition == null) return;
        if (addition.getType() != base.getType() && !isEnchantedBook(addition)) return;

        Map<Enchantment, Integer> baseEnch = enchantsOf(base);
        Map<Enchantment, Integer> addEnch = enchantsOf(addition);
        if (addEnch.isEmpty()) return;

        Map<Enchantment, Integer> merged = new LinkedHashMap<>(baseEnch);
        boolean anyOverVanillaCap = false;
        int overlevels = 0;

        for (Map.Entry<Enchantment, Integer> entry : addEnch.entrySet()) {
            Enchantment ench = entry.getKey();
            int addLevel = entry.getValue();
            int baseLevel = baseEnch.getOrDefault(ench, 0);

            boolean conflicts = merged.keySet().stream()
                    .anyMatch(existing -> !existing.equals(ench) && existing.conflictsWith(ench));
            if (conflicts) continue;

            int computed = (baseLevel > 0 && addLevel > 0)
                    ? (baseLevel == addLevel ? baseLevel + 1 : Math.max(baseLevel, addLevel))
                    : Math.max(baseLevel, addLevel);
            computed = Math.min(computed, config.capFor(ench));

            if (computed > ench.getMaxLevel()) {
                anyOverVanillaCap = true;
                overlevels += computed - Math.max(ench.getMaxLevel(), baseLevel);
            }
            merged.put(ench, computed);
        }

        // Within vanilla's own cap, vanilla already produces the right result - leave it alone.
        if (!anyOverVanillaCap) return;

        ItemStack result = base.clone();
        ItemMeta meta = result.getItemMeta();
        if (meta == null) return;
        applyEnchants(meta, merged);
        applyRename(event, meta);

        if (meta instanceof Repairable repairable) repairable.setRepairCost(0);
        result.setItemMeta(meta);

        repairDurability(base, addition, result);

        int cost = Math.max(1, overlevels * config.xpCostPerOverlevel());
        event.getView().setRepairCost(cost);
        if (event.getView().getMaximumRepairCost() < cost) {
            event.getView().setMaximumRepairCost(cost);
        }

        event.setResult(result);
    }

    private boolean isEnchantedBook(ItemStack item) {
        return item.getItemMeta() instanceof EnchantmentStorageMeta;
    }

    private Map<Enchantment, Integer> enchantsOf(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            return new LinkedHashMap<>(storageMeta.getStoredEnchants());
        }
        if (meta != null) {
            return new LinkedHashMap<>(meta.getEnchants());
        }
        return Map.of();
    }

    private void applyEnchants(ItemMeta meta, Map<Enchantment, Integer> merged) {
        if (meta instanceof EnchantmentStorageMeta storageMeta) {
            for (Enchantment e : new ArrayList<>(storageMeta.getStoredEnchants().keySet())) {
                storageMeta.removeStoredEnchant(e);
            }
            merged.forEach((e, level) -> {
                if (level > 0) storageMeta.addStoredEnchant(e, level, true);
            });
        } else {
            meta.removeEnchantments();
            merged.forEach((e, level) -> {
                if (level > 0) meta.addEnchant(e, level, true);
            });
        }
    }

    private void applyRename(PrepareAnvilEvent event, ItemMeta meta) {
        String rename = event.getView().getRenameText();
        if (rename != null && !rename.isBlank()) {
            meta.displayName(Component.text(rename).decoration(TextDecoration.ITALIC, false));
        }
    }

    /** Mirrors vanilla's anvil repair math (12% bonus durability) when both items are the same tool type. */
    private void repairDurability(ItemStack base, ItemStack addition, ItemStack result) {
        if (addition.getType() != base.getType()) return;
        short maxDurability = base.getType().getMaxDurability();
        if (maxDurability <= 0) return;

        ItemMeta resultMeta = result.getItemMeta();
        ItemMeta additionMeta = addition.getItemMeta();
        if (!(resultMeta instanceof Damageable resultDmg) || !(additionMeta instanceof Damageable additionDmg)) return;

        int repairAmount = (maxDurability - resultDmg.getDamage())
                + (maxDurability - additionDmg.getDamage())
                + (int) (maxDurability * 0.12);
        resultDmg.setDamage(Math.max(0, maxDurability - repairAmount));
        result.setItemMeta(resultMeta);
    }
}
