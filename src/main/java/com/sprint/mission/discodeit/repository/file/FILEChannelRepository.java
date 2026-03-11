package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FILEChannelRepository implements ChannelRepository {

    private final Path directory;

    public FILEChannelRepository() {

        directory = Path.of("src/main/resources/Channels/");
    }

    @Override
    public boolean saveChannel(Channel channel) {
        if(isExistChannel(channel.getChannelId())){
            return false;
        }

        save(pathToUserId(channel.getChannelId()),channel);
        return true;


    }

    @Override
    public Channel getChannel(String channelId) {
        Map<String,Channel> map = load(directory);

        return map.getOrDefault(channelId,null);
    }

    @Override
    public List<Channel> getAllChannel() {
        Map<String,Channel> map = load(directory);

        return map.values().stream().toList();
    }

    @Override
    public boolean updateChannel(Channel channel) {

        Map<String,Channel> map = load(directory);

        map.put(channel.getChannelId(), channel);

        save(pathToUserId(channel.getChannelId()),channel);


        return true;

    }

    @Override
    public boolean deleteChannel(String channelId) {

        if(!isExistChannel(channelId))
            return false;

        try {
            Files.deleteIfExists(pathToUserId(channelId));
        }
        catch(IOException e){
            return false;


        }
        return true;
    }


    @Override
    public boolean isExistChannel(String channelId) {
        Map<String, Channel> map = load(directory);

        return map.containsKey(channelId);

    }

    private Map<String,Channel> load(Path directory) {
        if (Files.exists(directory)) {


            try (Stream<Path> stream =  Files.list(directory))

            {
                Map<String,Channel> map;


                map = stream.map(path -> {
                            try (
                                    FileInputStream fis = new FileInputStream(path.toFile());
                                    ObjectInputStream ois = new ObjectInputStream(fis)
                            ) {
                                Object data = ois.readObject();
                                return  (Channel)data;
                            } catch (IOException | ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .collect(Collectors.toMap(
                                Channel::getChannelId,
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

    private void save(Path filePath, Channel channel) {
        try(
                FileOutputStream fos = new FileOutputStream(filePath.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            oos.writeObject(channel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    private Path pathToUserId(String channelId){

        return directory.resolve(channelId + ".dat");

    }











}
