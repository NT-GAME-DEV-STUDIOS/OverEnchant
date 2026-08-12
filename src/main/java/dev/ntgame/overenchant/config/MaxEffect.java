package dev.ntgame.overenchant.config;

/** Special behaviour that unlocks once an enchantment reaches its configured cap on an item. */
public enum MaxEffect {
    NONE,
    /** Efficiency at its cap breaks any block instantly, regardless of hardness. */
    INSTAMINE
}
