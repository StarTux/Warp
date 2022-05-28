package com.cavetale.warp;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.command.RemotePlayer;
import com.cavetale.core.connect.Connect;
import com.cavetale.core.event.player.PluginPlayerEvent.Detail;
import com.cavetale.core.event.player.PluginPlayerEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import static net.kyori.adventure.text.Component.join;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.space;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.JoinConfiguration.noSeparators;
import static net.kyori.adventure.text.JoinConfiguration.separator;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;
import static net.kyori.adventure.text.event.HoverEvent.showText;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.*;

public final class WarpCommand extends AbstractCommand<WarpPlugin> {
    protected WarpCommand(final WarpPlugin plugin) {
        super(plugin, "warp");
    }

    @Override
    protected void onEnable() {
        rootNode.arguments("[name]")
            .description("Use a warp")
            .completers((context, node, arg) -> {
                    List<String> result = new ArrayList<>();
                    String lower = arg.toLowerCase();
                    for (String key : plugin.warps.keys()) {
                        if (key.toLowerCase().contains(lower)) {
                            result.add(key);
                        }
                    }
                    return result;
                })
            .remotePlayerCaller(this::warp);
    }

    private boolean warp(RemotePlayer player, String[] args) {
        if (args.length == 0) {
            listWarps(player);
            return true;
        }
        final String name = String.join(" ", args);
        plugin.database.find(SQLWarp.class)
            .eq("name", name)
            .findUniqueAsync(warp -> warp2(player, name, warp));
        return true;
    }

    private void warp2(RemotePlayer player, String name, SQLWarp warp) {
        if (warp == null || !warp.hasPermission(player)) {
            player.sendMessage(text("Warp not found: " + name, RED));
            return;
        }
        if (player.isPlayer() && !warp.isOnThisServer()) {
            Connect.get().dispatchRemoteCommand(player.getPlayer(), "warp " + name, warp.getServer());
            return;
        }
        warp.toLocation(location -> warp3(player, warp, location));
    }

    private void warp3(RemotePlayer player, SQLWarp warp, Location location) {
        if (location == null) {
            player.sendMessage(text("Warp not found: " + warp.getName(), RED));
            return;
        }
        player.sendMessage(Component.text("Warping to " + warp.getName(), GREEN));
        player.bring(plugin, location, player2 -> {
                if (player2 == null) return;
                PluginPlayerEvent.Name.USE_WARP.make(plugin, player2)
                    .detail(Detail.NAME, warp.getName())
                    .detail(Detail.LOCATION, location)
                    .callEvent();
            });
    }

    public void listWarps(RemotePlayer sender) {
        plugin.database.find(SQLWarp.class).findListAsync(list -> listWarps2(sender, list));
    }

    private void listWarps2(RemotePlayer sender, List<SQLWarp> rows) {
        plugin.warps = new Warps(rows);
        List<String> keys = new ArrayList<>(plugin.warps.keys());
        Collections.sort(keys);
        int count = 0;
        Map<String, List<Component>> categoryMap = new HashMap<>();
        for (String key : keys) {
            SQLWarp warp = plugin.warps.get(key);
            if (!warp.hasPermission(sender)) continue;
            Component component = join(noSeparators(), text("[", WHITE), warp.parseTitle(), text("]", WHITE))
                .clickEvent(runCommand("/warp " + key))
                .hoverEvent(showText(join(separator(newline()), warp.getTooltip())));
            String category = warp.getCategory();
            categoryMap.computeIfAbsent(category, c -> new ArrayList<>()).add(component);
            count += 1;
        }
        if (count == 0) {
            sender.sendMessage(text("There are no warps", RED));
            return;
        } else if (count == 1) {
            sender.sendMessage(text("There is one warp: ", WHITE));
        } else {
            sender.sendMessage(text("There are " + count + " warps: ", WHITE));
        }
        List<String> categories = new ArrayList<>(categoryMap.keySet());
        Collections.sort(categories);
        for (String category : categories) {
            List<Component> components = categoryMap.get(category);
            if (!category.isEmpty()) {
                sender.sendMessage(text(category + " (" + components.size() + ")", AQUA, ITALIC));
            }
            sender.sendMessage(join(separator(space()), components));
        }
        if (sender.isPlayer()) {
            PluginPlayerEvent.Name.LIST_WARPS.call(plugin, sender.getPlayer());
        }
    }
}
