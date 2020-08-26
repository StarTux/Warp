package com.cavetale.warp;

import org.bukkit.plugin.java.JavaPlugin;

public final class WarpPlugin extends JavaPlugin {
    private final Json json = new Json(this);
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
        warps = json.load("warps.json", Warps.class, Warps::new);
    }

    void saveWarps() {
        if (warps == null) return;
        json.save("warps.json", warps, true);
    }
}
