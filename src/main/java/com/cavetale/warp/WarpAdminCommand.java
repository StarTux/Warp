package com.cavetale.warp;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.command.CommandArgCompleter;
import com.cavetale.core.command.CommandWarn;
import com.cavetale.core.connect.Connect;
import java.util.Arrays;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import static java.util.stream.Collectors.toList;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public final class WarpAdminCommand extends AbstractCommand<WarpPlugin> {
    protected WarpAdminCommand(final WarpPlugin plugin) {
        super(plugin, "warpadmin");
    }

    @Override
    protected void onEnable() {
        rootNode.addChild("set").arguments("<name>")
            .description("Set a warp")
            .completers(CommandArgCompleter.supplyList(() -> plugin.getWarps().keys()))
            .playerCaller(this::set);
        rootNode.addChild("permission").arguments("<name> [permission]")
            .description("Set warp permission")
            .completers(CommandArgCompleter.supplyList(() -> plugin.getWarps().keys()),
                        CommandArgCompleter.supplyList(() -> Bukkit.getPluginManager().getPermissions().stream()
                                                       .map(Permission::getName)
                                                       .collect(toList())))
            .playerCaller(this::permission);
        rootNode.addChild("category").arguments("<name> <category>")
            .description("Set warp category")
            .completers(CommandArgCompleter.supplyList(() -> plugin.getWarps().keys()),
                        CommandArgCompleter.EMPTY)
            .senderCaller(this::category);
        rootNode.addChild("delete").arguments("<name>")
            .description("Delete warp")
            .completers(CommandArgCompleter.supplyList(() -> plugin.getWarps().keys()))
            .senderCaller(this::delete);
        rootNode.addChild("send").arguments("<player> <warp>")
            .description("Send a player to a warp")
            .completers(CommandArgCompleter.ONLINE_PLAYERS,
                        CommandArgCompleter.supplyList(() -> plugin.getWarps().keys()))
            .senderCaller(this::send);
    }

    private boolean set(Player player, String[] args) {
        if (args.length != 1) return false;
        String name = args[0];
        plugin.database.saveAsync(new SQLWarp(name, player.getLocation()),
                                  Set.of("server", "world", "x", "y", "z", "pitch", "yaw", "updated"),
                                  r -> {
                                      plugin.broadcastUpdate();
                                      player.sendMessage(text("Warp created: " + name, AQUA));
                                  });
        return true;
    }

    private SQLWarp requireWarp(String name) {
        SQLWarp warp = plugin.warps.get(name);
        if (warp == null) throw new CommandWarn("Warp not found: " + name);
        return warp;
    }

    private boolean permission(CommandSender player, String[] args) {
        if (args.length > 2) return false;
        SQLWarp warp = requireWarp(args[0]);
        warp.setPermission(args.length >= 2 ? args[1] : "");
        plugin.database.updateAsync(warp, Set.of("permission"), r -> {
                plugin.broadcastUpdate();
                player.sendMessage(text("Permission of " + warp.getName() + " updated: " + warp.getPermission(), AQUA));
            });
        return true;
    }

    private boolean category(CommandSender player, String[] args) {
        if (args.length < 2) return false;
        SQLWarp warp = requireWarp(args[0]);
        warp.setCategory(String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
        plugin.database.updateAsync(warp, Set.of("category"), r -> {
                plugin.broadcastUpdate();
                player.sendMessage(text("Category of " + warp.getName() + " updated: " + warp.getCategory(), AQUA));
            });
        return true;
    }

    private boolean delete(CommandSender sender, String[] args) {
        if (args.length != 1) return false;
        String name = args[0];
        plugin.database.find(SQLWarp.class).eq("name", name).deleteAsync(r -> {
                if (r == 0) {
                    sender.sendMessage(text("Warp not found: " + name, RED));
                } else {
                    sender.sendMessage(text("Warp deleted: " + name, AQUA));
                    plugin.broadcastUpdate();
                }
            });
        return true;
    }

    private boolean send(CommandSender sender, String[] args) {
        if (args.length != 2) return false;
        final Player player = CommandArgCompleter.requirePlayer(args[0]);
        final SQLWarp warp = requireWarp(args[1]);
        if (!warp.isOnThisServer()) {
            Connect.get().sendMessage(warp.getServer(), ConnectSendMessage.CHANNEL_NAME, new ConnectSendMessage(player.getUniqueId(), warp.getName()).serialize());
            sender.sendMessage("Sending player to remote warp: " + player.getName() + " => " + warp.getName() + " (" + warp.getServer() + ")");
        } else {
            warp.toLocation(player::teleport);
            sender.sendMessage("Sending player to local warp: " + player.getName() + " => " + warp.getName());
        }
        return true;
    }
}
