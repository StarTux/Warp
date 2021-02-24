package com.cavetale.warp;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public final class SetWarpCommand implements TabExecutor {
    private final WarpPlugin plugin;

    public SetWarpCommand enable() {
        plugin.getCommand("setwarp").setExecutor(this);
        return this;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 1 && args[0].equals("-reload")) {
            plugin.loadWarps();
            sender.sendMessage("Warps reloaded");
            return true;
        }
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;
        if (args.length == 0) return false;
        String name = Stream.of(args).collect(Collectors.joining(" "));
        plugin.warps.set(name, new Warp(player.getLocation()));
        player.sendMessage("Warp created: " + name);
        plugin.saveWarps();
        return true;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        return Collections.emptyList();
    }
}
