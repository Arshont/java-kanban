package ru.yandex.javacourse.schedule.http.handlers.serializers_deserializers;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class EpicDeserializer implements JsonDeserializer<Epic> {
    @Override
    public Epic deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject json = jsonElement.getAsJsonObject();

        int id = json.get("id").getAsInt();
        String name = json.get("name").getAsString();
        String description = json.get("description").getAsString();

        Epic epic = new Epic(id, name, description);

        if (json.has("status")) {
            epic.setStatus(jsonDeserializationContext.deserialize(json.get("status"), TaskStatus.class));
        }

        if (json.has("startTime")) {
            epic.setStartTime(jsonDeserializationContext.deserialize(json.get("startTime"), LocalDateTime.class));
        }

        if (json.has("endTime")) {
            epic.setEndTime(jsonDeserializationContext.deserialize(json.get("endTime"), LocalDateTime.class));
        }

        if (json.has("subtaskIds")) {
            epic.subtaskIds = jsonDeserializationContext.deserialize(
                    json.get("subtaskIds"),
                    new TypeToken<ArrayList<Integer>>(){}.getType()
            );
        }

        return epic;
    }
}
