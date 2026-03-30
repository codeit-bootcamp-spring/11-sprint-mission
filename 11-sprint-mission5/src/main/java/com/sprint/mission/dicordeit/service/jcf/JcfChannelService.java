package com.sprint.mission.dicordeit.service.jcf;

import com.sprint.mission.dicordeit.service.ChannelService;
import com.sprint.mission.dicordeit.entity.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JcfChannelService implements ChannelService {
    private final List<Channel> data = new ArrayList<>();



    @Override
    public Channel createChannel(String name, String description) {
        Channel newChannel = new Channel(name, description);
        data.add(newChannel);

        return newChannel;
    }
    @Override
    public Channel readChannel(UUID channelId) {
        return data.stream()
                .filter(user -> user.getId().equals(channelId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Channel> allChannel() {
        return data;
    }

    @Override
    public Channel updateChannel(UUID channelId, String newChannel, String newDescription) {
        Channel newnameChannel = readChannel(channelId);
        if(newnameChannel != null){
            newnameChannel.update(newChannel,newDescription);
            return newnameChannel;
        }

        return null;
    }

    @Override
    public void deleteChannel(UUID channelId) {
         data.remove(readChannel(channelId));

    }
}
