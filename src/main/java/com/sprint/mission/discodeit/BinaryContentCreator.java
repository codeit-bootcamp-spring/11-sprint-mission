package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.BinaryContent;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class BinaryContentCreator {
    public static void main(String[] args) throws IOException {
        Path imagePath = Path.of("data/raw-images/rex.png");
        byte[] bytes = Files.readAllBytes(imagePath);
        String contentType = Files.probeContentType(imagePath);

        BinaryContent binaryContent = new BinaryContent("rex.png", contentType, bytes);

        Path outputDir = Path.of("data/binarycontents");
        Files.createDirectories(outputDir);

        Path outputFile = outputDir.resolve(binaryContent.getId().toString() + ".ser");

        try (
                FileOutputStream fos = new FileOutputStream(outputFile.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(binaryContent);
        }

        System.out.println("saved: " + outputFile);
        System.out.println("profileId: " + binaryContent.getId());
        System.out.println("contentType: " + contentType);
    }
}