package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FILEUserRepository  implements UserRepository {

    private final Path directory;


    public FILEUserRepository() {
        directory = Path.of("src/main/resources/Users/");
    }

    @Override
    public boolean saveUser(User user) {

        if(isExistUser(user.getUserId())){
            return false;
        }

        save(pathToUserId(user.getUserId()),user);
        return true;

    }

    @Override
    public User getUser(String userId) {

        Map<String,User> map = load(directory);

        return map.getOrDefault(userId,null);



    }

    @Override
    public List<User> getAllUser() {

        Map<String,User> map = load(directory);

        return map.values().stream().toList();

    }

    @Override
    public boolean updateUser(User user) {
        Map<String,User> map = load(directory);

        map.put(user.getUserId(),user);

        save(pathToUserId(user.getUserId()),user);
        return true;
    }

    @Override
    public boolean deleteUser(String userId) {


       if(!isExistUser(userId))
           return false;

       try {
           Files.deleteIfExists(pathToUserId(userId));
       }
       catch(IOException e){
           return false;


       }
       return true;



    }

    @Override
    public boolean isExistUser(String userId) {
        Map<String, User> map = load(directory);

        return map.containsKey(userId);

    }
    
     private Map<String,User> load(Path directory) {
        if (Files.exists(directory)) {


            try (Stream<Path> stream =  Files.list(directory))

            {
                Map<String,User> map;


                map = stream.map(path -> {
                        try (
                                    FileInputStream fis = new FileInputStream(path.toFile());
                                    ObjectInputStream ois = new ObjectInputStream(fis)
                            ) {
                                Object data = ois.readObject();
                                return  (User)data;
                            } catch (IOException | ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .collect(Collectors.toMap(
                                User::getUserId,
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

     private void save(Path filePath, User user) {
        try(
                FileOutputStream fos = new FileOutputStream(filePath.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(user);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    private Path pathToUserId(String userId){

        return directory.resolve(userId + ".dat");

    }


}
