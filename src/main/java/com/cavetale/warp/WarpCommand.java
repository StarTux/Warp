package com.cavetale.warp;

import com.cavetale.core.event.player.PluginPlayerEvent.Detail;
import com.cavetale.core.event.player.PluginPlayerEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
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
        Player player = sender instanceof Player ? (Player) sender : null;
        if (args.length == 0) {
            ComponentBuilder cb = new ComponentBuilder("There are " + plugin.warps.count() + " warps: ").color(ChatColor.AQUA);
            List<String> keys = new ArrayList<>(plugin.warps.keys());
            Collections.sort(keys);
            boolean comma = false;
            for (String key : plugin.warps.keys()) {
                if (comma) {
                    cb.append(", ").color(ChatColor.GRAY);
                } else {
                    comma = true;
                }
                cb.append(key).color(ChatColor.WHITE);
                cb.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/warp " + key));
                BaseComponent[] tooltip = new ComponentBuilder("/warp " + key).color(ChatColor.AQUA).create();
                cb.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip));
            }
            sender.sendMessage(cb.create());
            if (player != null) {
                PluginPlayerEvent.Name.LIST_WARPS.call(plugin, player);
            }
            return true;
        }
        if (args.length > 1) return false;
        if (player == null) return false;
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
        boolean allowed = PluginPlayerEvent.Name.USE_WARP.cancellable(plugin, player)
            .detail(Detail.NAME, name)
            .detail(Detail.LOCATION, loc)
            .call();
        if (!allowed) return true;
        loc.getWorld().getChunkAtAsync(loc.getBlockX() >> 4, loc.getBlockZ() >> 4, chunk -> {
                player.teleport(loc, PlayerTeleportEvent.TeleportCause.COMMAND);
                player.sendMessage(ChatColor.AQUA + "Warping: " + ChatColor.RESET + name);
            });
        return true;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 1) return plugin.warps.complete(args[0]);
        return Collections.emptyList();
    }
}
