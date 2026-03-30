package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.BinaryContent;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLOutput;

public class BinaryContentCreator {

    public static void createFunction(String fileName) throws IOException{
        Path imagePath = Path.of("data/raw-images/" + fileName);
        String contentType = Files.probeContentType(imagePath);
        byte[] bytes = Files.readAllBytes(imagePath);

        BinaryContent binaryContent = new BinaryContent(fileName, contentType, bytes);

        Path outputDir = Path.of("data/binarycontents");
        Files.createDirectories(outputDir);

        Path outputFile = outputDir.resolve(binaryContent.getId().toString() + ".ser");

        try (
                FileOutputStream fos = new FileOutputStream(outputFile.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(binaryContent);
        }

        System.out.println("name: " + fileName);
        System.out.println("saved: " + outputFile);
        System.out.println("profileId: " + binaryContent.getId());
        System.out.println("contentType: " + contentType);
        System.out.println();
    }

    public static void main(String[] args) throws IOException {
        createFunction("woody.png");
        createFunction("jessie.png");
        createFunction("buzz.png");
        createFunction("rex.png");
    }
}