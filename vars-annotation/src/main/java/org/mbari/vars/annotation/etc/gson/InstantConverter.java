package org.mbari.vars.annotation.etc.gson;

import com.google.gson.*;
import org.mbari.vars.annotation.etc.jdk.Instants;

import java.lang.reflect.Type;
import java.time.Instant;

public class InstantConverter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {



    @Override
    public Instant deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return Instants.parseIso8601(jsonElement.getAsString()).orElse(null);
    }

    @Override
    public JsonElement serialize(Instant instant, Type type, JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(instant.toString());
    }
}
