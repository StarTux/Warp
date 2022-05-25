package com.cavetale.warp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Just a cache!
 */
public final class Warps {
    private final Map<String, SQLWarp> warps = new HashMap<>();

    public Warps() { }

    public Warps(final List<SQLWarp> list) {
        for (SQLWarp it : list) {
            warps.put(it.getName(), it);
        }
    }

    public int count() {
        return warps.size();
    }

    public SQLWarp get(String name) {
        return warps.get(name);
    }

    public List<String> keys() {
        return new ArrayList<>(warps.keySet());
    }

    public List<SQLWarp> all() {
        return new ArrayList<>(warps.values());
    }
}
