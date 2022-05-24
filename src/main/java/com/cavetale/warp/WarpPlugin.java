package com.cavetale.warp;

import com.cavetale.core.util.Json;
import java.io.File;
import org.bukkit.plugin.java.JavaPlugin;

public final class WarpPlugin extends JavaPlugin {
    Warps warps;
    private WarpCommand warpCommand;
    private SetWarpCommand setWarpCommand;

    @Override
    public void onEnable() {
        warpCommand = new WarpCommand(this).enable();
        setWarpCommand = new SetWarpCommand(this).enable();
        loadWarps();
    }

    @Override
    public void onDisable() {
    }

    void loadWarps() {
        warps = Json.load(new File(getDataFolder(), "warps.json"), Warps.class, Warps::new);
    }

    void saveWarps() {
        if (warps == null) return;
        Json.save(new File(getDataFolder(), "warps.json"), warps, true);
    }
}
