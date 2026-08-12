package dev.ntgame.overenchant.commands;

import dev.ntgame.overenchant.OverEnchantPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class OverEnchantCommand implements CommandExecutor {

    private final OverEnchantPlugin plugin;

    public OverEnchantCommand(OverEnchantPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadOverEnchantConfig();
            sender.sendMessage(Component.text("OverEnchant config reloaded.", NamedTextColor.GREEN));
            return true;
        }
        sender.sendMessage(Component.text("Usage: /overenchant reload", NamedTextColor.RED));
        return true;
    }
}
