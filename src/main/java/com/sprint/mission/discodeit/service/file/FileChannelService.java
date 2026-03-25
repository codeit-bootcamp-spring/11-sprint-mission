//package com.sprint.mission.discodeit.service.file;
//
//import com.sprint.mission.discodeit.entity.Channel;
//import com.sprint.mission.discodeit.entity.ChannelType;
//import com.sprint.mission.discodeit.service.ChannelService;
//
//import java.io.*;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//public class FileChannelService implements ChannelService {
//    public static void saveChannels(List<Channel> channels) {
//        try (FileOutputStream fos = new FileOutputStream("channel.ser");
//             ObjectOutputStream oos = new ObjectOutputStream(fos);
//        ) {
//            oos.writeObject(channels);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private List<Channel> loadChannels() {
//        File file = new File("channel.ser");
//
//        if (!file.exists()) {
//            return new ArrayList<>();
//        }
//
//        try (FileInputStream fis = new FileInputStream("channel.ser");
//             ObjectInputStream ois = new ObjectInputStream(fis)) {
//            return (List<Channel>) ois.readObject();
//        } catch (IOException | ClassNotFoundException e) {
//            e.printStackTrace();
//            return new ArrayList<>();
//        }
//    }
//
//    @Override
//    public Channel create(ChannelType type, String name, String description) {
//        List<Channel> channels = loadChannels();
//        Channel channel = new Channel(type, name, description);
//        channels.add(channel);
//        saveChannels(channels);
//        return channel;
//    }
//
//    @Override
//    public Channel findById(UUID id) {
//        return loadChannels().stream()
//                .filter(channel -> channel.getId().equals(id))
//                .findFirst().orElse(null);
//    }
//
//    @Override
//    public List<Channel> findAll() {
//        return loadChannels();
//    }
//
//    @Override
//    public void update(UUID id, String name) {
//        List<Channel> channels = loadChannels();
//        boolean isUpdated = false;
//        for (Channel channel : channels) {
//            if (channel.getId().equals(id)) {
//                channel.update(name);
//                isUpdated = true;
//                break;
//            }
//        }
//        if(isUpdated) {
//            saveChannels(channels);
//        }
//    }
//
//    @Override
//    public void delete(UUID id) {
//        List<Channel> channels = loadChannels();
//        channels.removeIf(channel -> channel.getId().equals(id));
//        saveChannels(channels);
//    }
//}
