package dev.ntgame.overenchant.listeners;

import dev.ntgame.overenchant.OverEnchantPlugin;
import dev.ntgame.overenchant.config.EnchantRule;
import dev.ntgame.overenchant.config.MaxEffect;
import dev.ntgame.overenchant.config.OverEnchantConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Map;

/** Instantly breaks any block, regardless of hardness, once a tool's enchant reaches an INSTAMINE-tagged cap. */
public class InstamineListener implements Listener {

    private final OverEnchantPlugin plugin;

    public InstamineListener(OverEnchantPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        if (event.getInstaBreak()) return;
        OverEnchantConfig config = plugin.getOverEnchantConfig();

        Player player = event.getPlayer();
        if (!player.hasPermission("overenchant.use")) return;
        if (event.getBlock().getType().getHardness() < 0) return; // unbreakable, e.g. bedrock

        ItemStack tool = event.getItemInHand();
        ItemMeta meta = tool.getItemMeta();
        if (meta == null || !meta.hasEnchants()) return;

        for (Map.Entry<org.bukkit.enchantments.Enchantment, Integer> entry : meta.getEnchants().entrySet()) {
            EnchantRule rule = config.ruleFor(entry.getKey());
            if (rule != null && rule.effect() == MaxEffect.INSTAMINE && entry.getValue() >= rule.cap()) {
                event.setInstaBreak(true);
                return;
            }
        }
    }
}
