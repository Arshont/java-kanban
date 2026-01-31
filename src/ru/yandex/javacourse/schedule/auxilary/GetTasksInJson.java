package ru.yandex.javacourse.schedule.auxilary;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ru.yandex.javacourse.schedule.http.handlers.serializers_deserializers.EpicDeserializer;
import ru.yandex.javacourse.schedule.http.handlers.typeAdapters.DurationTypeAdapter;
import ru.yandex.javacourse.schedule.http.handlers.typeAdapters.LocalDateTimeTypeAdapter;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;

public class GetTasksInJson {

    public static void main(String[] args) {

        Duration basicTestDuration = Duration.ofMinutes(15);

        Gson gson = new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter().nullSafe())
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .registerTypeAdapter(Epic.class, new EpicDeserializer())
                .create();

//        Task task = new Task("Test 1", "Testing task 1", TaskStatus.NEW, basicTestDuration);
//        System.out.println(gson.toJson(task));
//
//        System.out.println(gson.fromJson(gson.toJson(task), Task.class));
        Epic epic = new Epic(0, "Epic 1", "Testing epic 1");
        System.out.println(gson.toJson(epic));
    }

}
