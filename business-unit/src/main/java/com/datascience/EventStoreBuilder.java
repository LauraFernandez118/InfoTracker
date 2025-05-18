package com.datascience;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class EventStoreBuilder {
    public static void saveToFile(String type, String topic, String content) throws IOException {
        String safeTopic = topic.replaceAll("[^a-zA-Z0-9]", "_").toLowerCase();
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        Path path = Paths.get("eventstore", safeTopic, type, date + ".events");

        Files.createDirectories(path.getParent());
        Files.writeString(path, content,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }
}