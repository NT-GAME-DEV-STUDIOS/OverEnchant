package dev.ntgame.overenchant.config;

import org.bukkit.enchantments.Enchantment;

/**
 * @param cap    highest level this enchantment can reach by combining items on an anvil
 * @param effect special behaviour unlocked once the enchantment reaches {@code cap} on an item
 */
public record EnchantRule(Enchantment enchantment, int cap, MaxEffect effect) {
}
