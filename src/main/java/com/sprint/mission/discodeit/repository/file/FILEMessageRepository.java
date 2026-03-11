package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FILEMessageRepository implements MessageRepository {

    private final Path directory;

    public FILEMessageRepository() {
        this.directory = Path.of("src/main/resources/Messages/");
    }

    @Override
    public boolean saveMessage(Message message) {

        if(isExistMessage(message.getMessageId())){
            return false;
        }
        save(pathToUserId(message.getMessageId()), message);
        return true;


    }

    @Override
    public Message getMessage(String messageId) {
        Map<String,Message> map = load(directory);
        return map.getOrDefault(messageId,null);
    }

    @Override
    public List<Message> getAllMessage() {
        Map<String,Message> map = load(directory);
        return map.values().stream().toList();
    }

    @Override
    public boolean updateMessage(Message message) {
        Map<String,Message> map = load(directory);
        map.put(message.getMessageId(), message);
        save(pathToUserId(message.getMessageId()),message);
        return true;
    }

    @Override
    public boolean deleteMessage(String messageId) {

        if(!isExistMessage(messageId))
            return false;
        try {
            Files.deleteIfExists(pathToUserId(messageId));
        }
        catch(IOException e){
            return false;
        }
        return true;
    }



    @Override
    public boolean isExistMessage(String messageId) {
        Map<String, Message> map = load(directory);

        return map.containsKey(messageId);

    }

    @Override
    public boolean channelsMessagedelete(String channelId) {
        Map<String, Message> map = load(directory);

        map.values().stream()
                .filter(msg-> msg.getChannelId().equals(channelId))
                .forEach(msg -> deleteMessage(msg.getMessageId()));

        return true;

    }

    private Map<String,Message> load(Path directory) {
        if (Files.exists(directory)) {


            try (Stream<Path> stream =  Files.list(directory))

            {
                Map<String,Message> map;


                map = stream.map(path -> {
                            try (
                                    FileInputStream fis = new FileInputStream(path.toFile());
                                    ObjectInputStream ois = new ObjectInputStream(fis)
                            ) {
                                Object data = ois.readObject();
                                return  (Message)data;
                            } catch (IOException | ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .collect(Collectors.toMap(
                                Message::getMessageId,
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

    private void save(Path filePath, Message message) {
        try(
                FileOutputStream fos = new FileOutputStream(filePath.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    private Path pathToUserId(String messageId){

        return directory.resolve(messageId + ".dat");

    }


}
