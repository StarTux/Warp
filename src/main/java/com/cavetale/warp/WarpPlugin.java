package com.cavetale.warp;

import com.winthier.sql.SQLDatabase;
import org.bukkit.plugin.java.JavaPlugin;

public final class WarpPlugin extends JavaPlugin {
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
    }
}
