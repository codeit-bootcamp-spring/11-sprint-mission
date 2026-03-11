package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Entity;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.UserService;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sprint.mission.discodeit.service.file.FILEUserService.isExistUser;

public class JCFChannelService implements ChannelService {

    Map<String,Channel > data;

    UserService userService;

    public JCFChannelService(UserService userService){

        data = new HashMap<>();
        this.userService = userService;

    }

    @Override
    public void createChannel(String channelName, String ownerID, String channelID) {



        if(isExistChannel(channelID)){
            System.out.println("이미 존재하는 채널 id입니다.");
            return;
        }


        Channel channel = new Channel(channelName,ownerID,channelID);

        data.put(channelID,channel);
        System.out.println(channel.getChannelName()+ " 채널 생성 완료!");


    }

    @Override
    public void readChannel(String channelId) {

        if(isExistChannel(channelId)){
            System.out.println(data.get(channelId));
            return;
        }

        System.out.println("없는 채널 아이디 입니다.");





    }

    @Override
    public void readAllChannel() {

        data.values().stream()
                .sorted(Entity::compareTo)
                .forEach(System.out::println);


    }

    @Override
    public void updateChannelName(String channelId, String channelName) {

        if(isExistChannel(channelId)){
            Channel channel = data.get(channelId);
            channel.updateChannelName(channelName);

            System.out.println("채널 이름"+ channel.getChannelName() + " (으)로 변경 완료!" );
            return;
        }
        System.out.println("존재하지 않는 채널 아이디입니다.");




    }

    @Override
    public void updateChannelOwner(String channelId, String ownerId) {
        if(isExistChannel(channelId)){
            Channel channel = data.get(channelId);

            List<String> members = channel.getMembers();

            if(!members.contains(ownerId)){
                System.out.println("해당 유저는 현재 채널에 없습니다.");
                return;


            }



            channel.updateOwner(ownerId);



            System.out.println("채널 주인 변경");
            return;

        }
        System.out.println("존재하지 않는 채널 아이디입니다");

    }

    @Override
    public void addMember(String channelId, String memberId) {
        if(isExistChannel(channelId)){
            Channel channel = data.get(channelId);


            if(!isExistUser(memberId)){
                System.out.println("존재하지 않는 멤버 아이디입니다.");
                return;

            }

            channel.addMember(memberId);

            System.out.println("새 멤버 추가 완료!");
            return;
        }
        System.out.println("존재하지 않는 채널 아이디입니다.");
    }



    @Override
    public void removeMember(String channelId, String memberId) {

        if(isExistChannel(channelId)){

            Channel channel = data.get(channelId);

            List<String> members = channel.getMembers();

            if(members.contains(memberId)){
                members.remove(memberId);
                System.out.println("멤버 제거 완료");




                if(members.isEmpty()){
                    channel.removeMember(memberId);
                }
                else if(channel.getOwnerId().equals(memberId)) {
                    String oid = members.get(0);
                    updateChannelOwner(channelId,oid);

                }




            }
            else {

                System.out.println("해당 채널에 존재하지 않는 유저입니다.");
            }
            return;

        }
        System.out.println("존재하지 않는 채널 아이디입니다.");

    }

    @Override
    public void deleteChannel(String channelId) {

        if(isExistChannel(channelId)){
            Channel channel = data.get(channelId);
            data.remove(channelId);
            System.out.println(channel.getChannelName() + " 채널 삭제 완료!");
            return;

        }

        System.out.println("존재하지 않는 채널 아이디 입니다.");


    }

    public boolean isExistChannel(String channelId){

        if(data.get(channelId) == null){
            return false;
        }
        return true;


    }

    public boolean isChannelsMember(String channelId, String memberId){


        if(!isExistChannel(channelId))
            return false;


        Channel channel = data.get(channelId);
        return channel.getMembers().contains(memberId);







    }
}
