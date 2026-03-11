package com.sprint.mission.discodeit.service.file;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class FILEServiceSystem {



    public static <T> void save(Path filePath, T data) {
        try(
                FileOutputStream fos = new FileOutputStream(filePath.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> load(Path directory) {
        if (Files.exists(directory)) {
            try {
                List<T> list;
                list = Files.list(directory)
                        .map(path -> {
                            try (
                                    FileInputStream fis = new FileInputStream(path.toFile());
                                    ObjectInputStream ois = new ObjectInputStream(fis)
                            ) {
                                Object data = ois.readObject();
                                return  (T) data;
                            } catch (IOException | ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .toList();
                return list;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return new ArrayList<>();
        }
    }


    public static boolean delete(Path filePath){

        File file = filePath.toFile();
        return file.delete();


    }






}
