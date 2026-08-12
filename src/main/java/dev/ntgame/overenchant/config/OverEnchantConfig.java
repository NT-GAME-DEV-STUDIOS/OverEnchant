package dev.ntgame.overenchant.config;

import dev.ntgame.overenchant.OverEnchantPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/** Loads {@code config.yml}: per-enchantment anvil caps plus the anvil XP-cost setting. */
public class OverEnchantConfig {

    /** Every leveled vanilla enchantment config.yml is allowed to reference, keyed by its vanilla id. */
    private static final Map<String, Enchantment> KNOWN_ENCHANTMENTS = new HashMap<>();

    static {
        register(Enchantment.PROTECTION);
        register(Enchantment.FIRE_PROTECTION);
        register(Enchantment.FEATHER_FALLING);
        register(Enchantment.BLAST_PROTECTION);
        register(Enchantment.PROJECTILE_PROTECTION);
        register(Enchantment.RESPIRATION);
        register(Enchantment.THORNS);
        register(Enchantment.DEPTH_STRIDER);
        register(Enchantment.SHARPNESS);
        register(Enchantment.SMITE);
        register(Enchantment.BANE_OF_ARTHROPODS);
        register(Enchantment.KNOCKBACK);
        register(Enchantment.LOOTING);
        register(Enchantment.SWEEPING_EDGE);
        register(Enchantment.EFFICIENCY);
        register(Enchantment.UNBREAKING);
        register(Enchantment.FORTUNE);
        register(Enchantment.POWER);
        register(Enchantment.PUNCH);
        register(Enchantment.LUCK_OF_THE_SEA);
        register(Enchantment.LURE);
        register(Enchantment.LOYALTY);
        register(Enchantment.IMPALING);
        register(Enchantment.RIPTIDE);
        register(Enchantment.PIERCING);
        register(Enchantment.SOUL_SPEED);
        register(Enchantment.SWIFT_SNEAK);
    }

    private static void register(Enchantment enchantment) {
        if (enchantment != null) KNOWN_ENCHANTMENTS.put(enchantment.getKey().getKey(), enchantment);
    }

    private final Map<Enchantment, EnchantRule> rules = new LinkedHashMap<>();
    private int xpCostPerOverlevel = 3;

    public static OverEnchantConfig load(OverEnchantPlugin plugin) {
        OverEnchantConfig config = new OverEnchantConfig();
        plugin.reloadConfig();
        var root = plugin.getConfig();

        config.xpCostPerOverlevel = Math.max(0, root.getInt("anvil.xp-cost-per-overlevel", 3));

        ConfigurationSection section = root.getConfigurationSection("enchantments");
        if (section == null) return config;

        for (String key : section.getKeys(false)) {
            Enchantment enchantment = KNOWN_ENCHANTMENTS.get(key.toLowerCase());
            if (enchantment == null) {
                plugin.getLogger().warning("Unknown enchantment key in config.yml: '" + key + "' - skipping.");
                continue;
            }
            ConfigurationSection entry = section.getConfigurationSection(key);
            if (entry == null) continue;

            int cap = entry.getInt("cap", enchantment.getMaxLevel());
            if (cap < enchantment.getMaxLevel()) cap = enchantment.getMaxLevel();

            MaxEffect effect = MaxEffect.NONE;
            String effectName = entry.getString("max-effect", "none");
            try {
                effect = MaxEffect.valueOf(effectName.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Unknown max-effect '" + effectName + "' for enchantment '" + key + "' - defaulting to none.");
            }

            config.rules.put(enchantment, new EnchantRule(enchantment, cap, effect));
        }
        return config;
    }

    public EnchantRule ruleFor(Enchantment enchantment) {
        return rules.get(enchantment);
    }

    public int capFor(Enchantment enchantment) {
        EnchantRule rule = rules.get(enchantment);
        return rule != null ? rule.cap() : enchantment.getMaxLevel();
    }

    public int xpCostPerOverlevel() {
        return xpCostPerOverlevel;
    }

    public Map<Enchantment, EnchantRule> rules() {
        return rules;
    }
}
