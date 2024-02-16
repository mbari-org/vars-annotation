package org.mbari.vars.services.gson;

import com.google.gson.*;
import org.mbari.vars.core.util.InstantUtils;

import java.lang.reflect.Type;
import java.time.Instant;

public class InstantConverter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {



    @Override
    public Instant deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return InstantUtils.parseIso8601(jsonElement.getAsString()).orElse(null);
    }

    @Override
    public JsonElement serialize(Instant instant, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(instant.toString());
    }
}
