package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;

import java.nio.file.Path;
import java.util.List;

import static com.sprint.mission.discodeit.service.file.FILEUserService.getUserToId;
import static com.sprint.mission.discodeit.service.file.FILEUserService.isExistUser;

public class FILEChannelService extends FILEServiceSystem implements ChannelService {


    private final Path directory = Path.of("src/main/resources/Channels/");

    public FILEChannelService() {

    }

    @Override
    public void createChannel(String channelName, String ownerID, String channelId) {

        if(isExistChannel(channelId)){
            System.out.println("이미 존재하는 채널 아이디입니다.");
            return;

        }
        if(!isExistUser(ownerID)){
            System.out.println("존재하지 않는 유저 아이디 입니다");
            return;

        }

        Channel channel = new Channel(channelName,ownerID,channelId);

        save(getPathToId(channelId),channel);

        System.out.println(channel.getChannelName()+ " 채널 생성 완료!");



    }

    @Override
    public void readChannel(String channelId) {

        Channel channel = getChannelToId(channelId);

        if(channel == null){

            return;

        }
        System.out.println(channel);


    }

    @Override
    public void readAllChannel() {

        List<Channel> Channels =  load(directory);

        Channels.stream()
                .sorted(Channel::compareTo)
                .forEach(System.out::println);


    }

    @Override
    public void updateChannelName(String channelId,String channelName) {

        Channel channel = getChannelToId(channelId);

        if(channel == null){

            return;
        }

        User user;
        user = getUserToId(channel.getOwnerId());



        if (user == null) {
            throw new RuntimeException("유효하지 않은 owner ID");
        }

        channel.updateChannelName(channelName);

        save(getPathToId(channelId),channel);

        System.out.println("채널 이름 업데이트 완료!");


    }

    @Override
    public void updateChannelOwner(String channelId, String ownerId) {

        Channel channel = getChannelToId(channelId);

        if(channel == null){
            return;

        }

        channel.updateOwner(ownerId);


        save(getPathToId(channelId),channel);

        System.out.println("채널장 업데이트 완료!");




    }

    @Override
    public void addMember(String channelId, String memberId) {

        Channel channel = getChannelToId(channelId);

        if(channel == null){
            return;
        }


        channel.addMember(memberId);

        save(getPathToId(channelId),channel);

        System.out.println(channel.getChannelName() +"채널에 멤버 추가 완료!");


    }

    @Override
    public void removeMember(String channelId, String memberId) {

        Channel channel = getChannelToId(channelId);

        if(channel == null){

            return;
        }

        if(channel.removeMember(memberId) == 0)
        {
            System.out.println("채널에서 유저가 나갔습니다!");
            delete(getPathToId(channelId));
            return;


        }



        save(getPathToId(channelId),channel);

        System.out.println("채널에서 유저가 나갔습니다!");

    }

    @Override
    public void deleteChannel(String channelId) {

        if(!isExistChannel(channelId)){
            System.out.println("존재하지 않는 채널 아이디입니다.");
            return;

        }
        delete(getPathToId(channelId));


    }


    @Override
    public boolean isExistChannel(String channelId) {

        List<Channel> channels =  load(directory);

        for(Channel channel : channels){

            if(channel.getChannelId().equals(channelId)){
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isChannelsMember(String channelId, String memberId) {

        if(!isExistChannel(channelId))
            return false;


        List<Channel> Channels =  load(directory);

        Channel channel = Channels.stream().
                filter(c -> c.getChannelId().equals(channelId)).
                findFirst().orElse(null);





        return false;
    }

    Channel getChannelToId(String channelId){

        List<Channel> channels =  load(directory);
        Channel channel;


        channel = channels.stream()
                    .filter(c -> c.getChannelId().equals(channelId))
                    .findFirst()
                    .orElse(null);


        if(channel == null){
            System.out.println("존재하지 않는 채널 아이디입니다.");

        }

        return channel;









    }

    Path getPathToId(String channelId){
        return directory.resolve(channelId+".dat");
    }
}

