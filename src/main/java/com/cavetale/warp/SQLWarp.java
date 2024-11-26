package com.cavetale.warp;

import com.cavetale.core.connect.Connect;
import com.cavetale.core.connect.ServerCategory;
import com.cavetale.core.worlds.Worlds;
import com.cavetale.mytems.Mytems;
import com.winthier.sql.SQLRow;
import com.winthier.sql.SQLRow.Name;
import com.winthier.sql.SQLRow.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permissible;
import static com.cavetale.core.font.Unicode.tiny;
import static com.cavetale.core.util.CamelCase.toCamelCase;
import static com.cavetale.mytems.util.Text.wrapLore;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.*;
import static net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson;

@Data @NotNull @Name("warps")
public final class SQLWarp implements SQLRow {
    @Id private Integer id;
    @VarChar(40) @Unique private String name;
    @VarChar(40) private String server;
    @VarChar(40) private String world;
    private double x;
    private double y;
    private double z;
    @Default("0") private float pitch;
    @Default("0") private float yaw;
    @VarChar(255) @Default("'Survival'") private String category;
    @VarChar(255) @Default("''") private String permission;
    @Text @Default("''") private String title; // component
    @VarChar(255) @Default("''") private String description;
    @VarChar(255) @Default("''") private String icon;
    @Default("0") private boolean hidden;
    @Default("NOW()") private Date created;
    @Default("NOW()") private Date updated;

    public SQLWarp() { }

    public SQLWarp(final String name, final Location location) {
        this.name = name;
        setLocation(location);
        this.category = toCamelCase(" ", ServerCategory.current());
        this.permission = "";
        this.title = "";
        this.description = "";
        this.icon = "";
        this.created = new Date();
        this.updated = new Date();
    }

    public void setLocation(Location location) {
        this.server = Connect.get().getServerName();
        this.world = location.getWorld().getName();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.pitch = location.getPitch();
        this.yaw = location.getYaw();
    }

    public Location toLocation(World w) {
        return new Location(w, x, y, z, yaw, pitch);
    }

    public void toLocation(Consumer<Location> callback) {
        Worlds.get().loadWorld(world, loadedWorld -> {
                callback.accept(loadedWorld != null
                                ? toLocation(loadedWorld)
                                : null);
            });
    }

    public boolean isOnThisServer() {
        return Connect.get().getServerName().equals(server);
    }

    public boolean hasPermission(Permissible who) {
        return permission.isEmpty() || who.hasPermission(permission);
    }

    public Component parseTitle() {
        return title.isEmpty()
            ? text(name, permission != null && !permission.isEmpty() ? RED : GREEN)
            : gson().deserialize(title);
    }

    public ItemStack parseIcon() {
        if (icon.isEmpty()) {
            return new ItemStack(Material.ENDER_PEARL);
        }
        if (icon.startsWith("mytems:")) {
            final Mytems mytems = Mytems.forId(icon.substring(7));
            if (mytems != null) {
                return mytems.createIcon();
            }
        }
        if (icon.startsWith("item:")) {
            try {
                return Bukkit.getItemFactory().createItemStack(icon.substring(5));
            } catch (IllegalArgumentException iae) { }
        }
        return Mytems.QUESTION_MARK.createIcon();
    }

    public List<Component> getTooltip() {
        List<Component> result = new ArrayList<>();
        result.add(parseTitle());
        if (!category.isEmpty()) {
            result.add(text(category, DARK_GRAY, ITALIC));
        }
        result.add(text("/warp " + name, GREEN));
        if (!description.isEmpty()) {
            result.addAll(wrapLore(tiny(description), c -> c.color(GRAY)));
        }
        return result;
    }
}
