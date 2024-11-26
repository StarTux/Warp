package com.cavetale.warp;

import com.cavetale.core.util.Json;
import java.io.Serializable;
import java.util.UUID;
import lombok.Value;

@Value
public final class ConnectSendMessage implements Serializable {
    public static final String CHANNEL_NAME = "warp:send";
    private final UUID player;
    private final String warpName;

    public String serialize() {
        return Json.serialize(this);
    }

    public static ConnectSendMessage deserialize(String json) {
        return Json.deserialize(json, ConnectSendMessage.class);
    }
}
