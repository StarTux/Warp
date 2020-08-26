package com.cavetale.warp;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

@RequiredArgsConstructor
public final class WarpCommand implements TabExecutor {
    private final WarpPlugin plugin;

    public WarpCommand enable() {
        plugin.getCommand("warp").setExecutor(this);
        return this;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.AQUA + "There are "
                               + plugin.warps.count() + " warps: "
                               + plugin.warps.keys().stream()
                               .sorted()
                               .map(s -> ChatColor.WHITE + s)
                               .collect(Collectors.joining(ChatColor.GRAY + ", ")));
            return true;
        }
        if (args.length > 1) return false;
        if (!(sender instanceof Player)) return false;
        Player player = (Player) sender;
        String name = args[0];
        Warp warp = plugin.warps.get(name);
        if (warp == null) {
            player.sendMessage(ChatColor.RED + "Warp not found: " + name);
            return true;
        }
        Location loc = warp.toLocation();
        if (loc == null) {
            player.sendMessage(ChatColor.RED + "Warp not found: " + name);
            return true;
        }
        player.teleport(loc, PlayerTeleportEvent.TeleportCause.COMMAND);
        player.sendMessage(ChatColor.AQUA + "Warping: " + ChatColor.RESET + name);
        return true;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 1) return plugin.warps.complete(args[0]);
        return Collections.emptyList();
    }
}
