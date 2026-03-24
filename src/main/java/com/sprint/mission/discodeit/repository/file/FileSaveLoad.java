package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Entity;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileSaveLoad<T extends Entity> {


    public Map<UUID, T> load(Path directory) {
        if (Files.exists(directory)) {


            try (Stream<Path> stream =  Files.list(directory))

            {
                Map<UUID,T> map;


                map = stream.map(path -> {
                            try (
                                    FileInputStream fis = new FileInputStream(path.toFile());
                                    ObjectInputStream ois = new ObjectInputStream(fis)
                            ) {
                                Object data = ois.readObject();
                                return  (T)data;
                            } catch (IOException | ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .collect(Collectors.toMap(
                                T::getId,
                                Function.identity()

                        ));
                return map;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return new HashMap<>();
        }
    }

    public void save(Path filePath, T typeParam) {

        try{
            Files.createDirectories(filePath.getParent());
        }
        catch(IOException e){
            throw new RuntimeException(e);
        }

        try(


                FileOutputStream fos = new FileOutputStream(filePath.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {

            oos.writeObject(typeParam);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }




















}
