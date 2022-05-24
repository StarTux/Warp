package com.cavetale.warp;

import com.cavetale.core.command.RemotePlayer;
import com.cavetale.core.connect.Connect;
import com.cavetale.core.event.player.PluginPlayerEvent.Detail;
import com.cavetale.core.event.player.PluginPlayerEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Chunk;
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
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length == 0) {
            listWarps(sender);
            return true;
        }
        if (sender instanceof Player player) return onCommand(player, label, args);
        if (sender instanceof RemotePlayer player) return onCommand(player, label, args);
        sender.sendMessage("[warp:warp] Player expected");
        return true;
    }

    public boolean onCommand(final Player player, final String label, final String[] args) {
        if (args.length > 1) return false;
        String name = args[0];
        if (name.contains(":")) {
            String[] toks = name.split(":", 2);
            String targetServer = toks[0];
            String warpName = toks[1];
            Connect.get().dispatchRemoteCommand(player, label + " " + warpName, targetServer);
            return true;
        }
        Warp warp = plugin.warps.get(name);
        if (warp == null) {
            player.sendMessage(Component.text("Warp not found: " + name, NamedTextColor.RED));
            return true;
        }
        Location loc = warp.toLocation();
        if (loc == null) {
            player.sendMessage(Component.text("Warp not found: " + name, NamedTextColor.RED));
            return true;
        }
        boolean allowed = PluginPlayerEvent.Name.USE_WARP.cancellable(plugin, player)
            .detail(Detail.NAME, name)
            .detail(Detail.LOCATION, loc)
            .call();
        if (!allowed) return true;
        loc.getWorld().getChunkAtAsync(loc.getBlockX() >> 4, loc.getBlockZ() >> 4, (Consumer<Chunk>) chunk -> {
                player.teleport(loc, PlayerTeleportEvent.TeleportCause.COMMAND);
                player.sendMessage(Component.text("Warping to " + name, NamedTextColor.GREEN));
            });
        return true;
    }

    public boolean onCommand(final RemotePlayer player, final String label, final String[] args) {
        if (args.length > 1) return false;
        String name = args[0];
        Warp warp = plugin.warps.get(name);
        if (warp == null) {
            player.sendMessage(Component.text("Warp not found: " + name, NamedTextColor.RED));
            return true;
        }
        Location location = warp.toLocation();
        if (location == null) {
            player.sendMessage(Component.text("Warp not found: " + name, NamedTextColor.RED));
            return true;
        }
        player.sendMessage(Component.text("Warping to " + name, NamedTextColor.GREEN));
        location.getWorld().getChunkAtAsync(location.getBlockX() >> 4, location.getBlockZ() >> 4, (Consumer<Chunk>) chunk -> {
                chunk.addPluginChunkTicket(plugin);
                player.bring(plugin, location, player2 -> { });
            });
        return true;
    }

    public void listWarps(CommandSender sender) {
        List<String> keys = new ArrayList<>(plugin.warps.keys());
        Collections.sort(keys);
        List<ComponentLike> components = new ArrayList<>();
        for (String key : plugin.warps.keys()) {
            Component tooltip = Component.text("/warp " + key, NamedTextColor.GREEN);
            components.add(Component.text().color(NamedTextColor.GREEN).content(key)
                           .clickEvent(ClickEvent.runCommand("/warp " + key))
                           .hoverEvent(HoverEvent.showText(tooltip)));
        }
        Component prefix = Component.text("There are " + plugin.warps.count() + " warps: ", NamedTextColor.WHITE);
        Component separator = Component.text(", ", NamedTextColor.GRAY);
        sender.sendMessage(Component.join(JoinConfiguration.builder().prefix(prefix).separator(separator).build(), components));
        if (sender instanceof Player) {
            PluginPlayerEvent.Name.LIST_WARPS.call(plugin, (Player) sender);
        }
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 1) return plugin.warps.complete(args[0]);
        return Collections.emptyList();
    }
}
