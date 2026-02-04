package ru.yandex.javacourse.schedule.http.handlers;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Adapters {
    static class LocalDateTimeTypeAdapter extends TypeAdapter<LocalDateTime> {

        private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy:HH:mm:ss");

        @Override
        public void write(JsonWriter jsonWriter, LocalDateTime localDateTime) throws IOException {
            jsonWriter.value(localDateTime.format(dtf));
        }

        @Override
        public LocalDateTime read(JsonReader jsonReader) throws IOException {
            return LocalDateTime.parse(jsonReader.nextString(), dtf);
        }
    }
}
