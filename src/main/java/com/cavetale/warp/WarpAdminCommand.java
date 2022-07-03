package com.cavetale.warp;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.connect.Connect;
import com.cavetale.core.util.Json;
import java.io.File;
import java.util.Date;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
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
            .playerCaller(this::set);
        rootNode.addChild("delete").arguments("<name>")
            .description("Delete a warp")
            .playerCaller(this::delete);
    }

    private boolean set(Player player, String[] args) {
        if (args.length == 0) return false;
        String name = String.join(" ", args);
        plugin.database.save(new SQLWarp(name, player.getLocation()),
                             "server", "world", "x", "y", "z", "pitch", "yaw", "updated");
        player.sendMessage(text("Warp created: " + name, AQUA));
        return true;
    }

    private boolean delete(CommandSender sender, String[] args) {
        if (args.length == 0) return false;
        String name = String.join(" ", args);
        plugin.database.find(SQLWarp.class).eq("name", name).deleteAsync(r -> {
                if (r == 0) {
                    sender.sendMessage(text("Warp not found: " + name, RED));
                } else {
                    sender.sendMessage(text("Warp deleted: " + name, AQUA));
                }
            });
        return true;
    }
}
