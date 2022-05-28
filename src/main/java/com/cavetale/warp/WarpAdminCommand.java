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
        rootNode.addChild("migrate").denyTabCompletion()
            .description("Migrate old warps")
            .senderCaller(this::migrate);
    }

    private boolean set(Player player, String[] args) {
        if (args.length == 0) return false;
        String name = String.join(" ", args);
        plugin.database.save(new SQLWarp(name, player.getLocation()),
                             "server", "world", "x", "y", "z", "pitch", "yaw", "updated");
        player.sendMessage(text("Warp created: " + name, GREEN));
        return true;
    }

    private void migrate(CommandSender sender) {
        File file;
        file = new File(plugin.getDataFolder(), "warps.json");
        int count = 0;
        if (file.exists()) {
            sender.sendMessage(text("Migrating old warps...", AQUA));
            Warps warps = Json.load(file, Warps.class, Warps::new);
            for (String key : warps.keys()) {
                SQLWarp warp = warps.get(key);
                warp.setName(key);
                warp.setCategory("Survival");
                warp.setPermission("");
                warp.setTitle("");
                warp.setDescription("");
                warp.setIcon("");
                warp.setServer(Connect.get().getServerName());
                warp.setCreated(new Date());
                warp.setUpdated(new Date());
            }
            plugin.database.save(warps.all());
            count += warps.count();
        }
        file = new File(Bukkit.getPluginsFolder(), "Creative/warps.yml");
        if (file.exists()) {
            sender.sendMessage(text("Migrating creative warps...", AQUA));
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            for (String key : config.getKeys(false)) {
                ConfigurationSection section = config.getConfigurationSection(key);
                SQLWarp warp = new SQLWarp();
                warp.setName(key);
                warp.setCategory("Creative");
                warp.setPermission("");
                warp.setTitle("");
                warp.setDescription("");
                warp.setIcon("");
                warp.setServer(Connect.get().getServerName());
                warp.setWorld(section.getString("world"));
                warp.setX(section.getDouble("x"));
                warp.setY(section.getDouble("y"));
                warp.setZ(section.getDouble("z"));
                warp.setYaw((float) section.getDouble("yaw"));
                warp.setPitch((float) section.getDouble("pitch"));
                warp.setCreated(new Date());
                warp.setUpdated(new Date());
                plugin.database.save(warp);
                count += 1;
            }
        }
        sender.sendMessage(text("Imported " + count + " warp(s)", AQUA));
    }
}
