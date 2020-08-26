package com.cavetale.warp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class Warps {
    private Map<String, Warp> warps = new HashMap<>();;

    public int count() {
        return warps.size();
    }

    public Warp get(String name) {
        return warps.get(name);
    }

    public void set(String name, Warp warp) {
        warps.put(name, warp);
    }

    public Set<String> keys() {
        return new HashSet<>(warps.keySet());
    }

    public List<Warp> all() {
        return new ArrayList<>(warps.values());
    }

    public List<String> complete(String arg) {
        String lower = arg.toLowerCase();
        return keys().stream()
            .filter(k -> k.toLowerCase().startsWith(lower))
            .collect(Collectors.toList());
    }
}
