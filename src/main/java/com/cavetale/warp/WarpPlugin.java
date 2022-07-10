package com.cavetale.warp;

import com.cavetale.core.connect.Connect;
import com.cavetale.core.event.connect.ConnectMessageEvent;
import com.winthier.sql.SQLDatabase;
import lombok.Getter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class WarpPlugin extends JavaPlugin implements Listener {
    protected Warps warps;
    private final WarpCommand warpCommand = new WarpCommand(this);
    private final WarpAdminCommand warpAdminCommand = new WarpAdminCommand(this);
    protected final SQLDatabase database = new SQLDatabase(this);

    @Override
    public void onEnable() {
        database.registerTable(SQLWarp.class);
        if (!database.createAllTables()) {
            throw new IllegalStateException("Database setup failed");
        }
        warpCommand.enable();
        warpAdminCommand.enable();
        warps = new Warps(database.find(SQLWarp.class).findList());
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    private void onConnectMessage(ConnectMessageEvent event) {
        if ("warp:update".equals(event.getChannel())) {
            database.find(SQLWarp.class).findListAsync(list -> warps = new Warps(list));
        }
    }

    protected void broadcastUpdate() {
        Connect.get().broadcastMessageToAll("warp:update", "");
    }
}
