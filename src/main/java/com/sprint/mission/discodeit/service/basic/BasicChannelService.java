package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.List;

public class BasicChannelService implements ChannelService {

    ChannelRepository channelRepository;
    UserRepository userRepository;
    MessageRepository messageRepository;

    public BasicChannelService(ChannelRepository channelRepository, UserRepository userRepository, MessageRepository messageRepository) {
        this.channelRepository = channelRepository;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    public void createChannel(String channelName, String ownerID, String channelId) {


        //채널 생성
        Channel channel = new Channel(channelName,ownerID,channelId);

        //채널 주인 가져오기
        User user = userRepository.getUser(ownerID);

        //디폴트 메시지 생성
        Message msg = new Message(ownerID,channelId,"default message");
        msg.setStatus(Message.messageStatus.INACTIVE);

        //유효 유저 확인
        if(user == null){
            System.out.println("존재하지 않는 유저입니다.");
            return;
        }

        //디폴트 메시지 추가
        user.addDefaultMessage(msg);


        //채널 저장.
        if(!channelRepository.saveChannel(channel)){
            System.out.println("이미 존재하는 채널입니다.");
            return;

        }

        //디폴트 메시지 저장, 유저 업데이트
        userRepository.updateUser(user);
        messageRepository.saveMessage(msg);

        System.out.println(channelName + " 채널 생성 완료!");



    }

    @Override
    public void readChannel(String channelId) {

        //채널 가져오기
        Channel channel = channelRepository.getChannel(channelId);

        //채널 여부 체크
        if(channel == null){
            System.out.println("존재하지 않는 채널입니다.");
            return;
        }

        //출력
        System.out.println(channel);


    }

    @Override
    public void readAllChannel() {

        //채널 가져오기
        List<Channel> channels = channelRepository.getAllChannel();

        //리스트 null 체크
        if(channels == null){
            System.out.println("채널 리스트를 불러오는데 문제가 발생했습니다.");
            return;
        }

        //출력
        channels.stream()
                .sorted(Channel::compareTo)
                .forEach(System.out::println);

    }

    @Override
    public void updateChannelName(String channelId, String channelName) {


        Channel channel = channelRepository.getChannel(channelId);

        //채널 여부 체크
        if(channel == null){
            System.out.println("존재하지 않는 채널입니다.");
            return;
        }
        //출력용 예전 닉네임
        String oldName = channel.getChannelName();

        //TODO : 비밀번호 체크 로직 넣기. 인터페이스 및 이전 코드들 약간씩 수정 필요

//        User user = userRepository.getUser(channel.getOwnerId());
//
//        if(!user.checkSamePassword(password)){
//
//            System.out.println("비밀번호가 일치하지 않습니다.");
//            return;
//
//        }

        //채널 인스턴스 수정
        channel.updateChannelName(channelName);


        //repository에 반영
        if(!channelRepository.updateChannel(channel)){

            System.out.println("채널 업데이트 중 문제가 발생했습니다.");
        }



        System.out.println("채널 이름 변경 완료! " + oldName  + " -> " + channel.getChannelName() );

    }

    @Override
    public void updateChannelOwner(String channelId, String ownerId) {

        Channel channel = channelRepository.getChannel(channelId);

        //채널 여부 체크
        if(channel == null){
            System.out.println("존재하지 않는 채널입니다.");
            return;
        }


        //TODO : 비밀번호 체크 로직 넣기. 인터페이스 및 이전 코드들 약간씩 수정 필요



        //채널 멤버인지 체크
        List<String> channelsUserIdList = channel.getMembers();


        if(!channelsUserIdList.contains(ownerId)){
            System.out.println("해당 유저가 채널에 없습니다.");
            return;
        }



        //채널 인스턴스 수정
        channel.updateOwner(ownerId);


        //repository에 반영
        if(!channelRepository.updateChannel(channel)){

            System.out.println("채널 업데이트 중 문제가 발생했습니다.");
            return;
        }


        User member = userRepository.getUser(ownerId);

        System.out.println(channel.getChannelName() +  " 채널에서 " + member.getNickname() + " 님이 채널장이 되셨습니다.");




    }

    @Override
    public void addMember(String channelId, String memberId) {


        Channel channel = channelRepository.getChannel(channelId);

        //채널 여부 체크
        if(channel == null){
            System.out.println("존재하지 않는 채널입니다.");
            return;
        }

        //유저 가져오기
        User user = userRepository.getUser(memberId);

        //유효 유저 확인
        if(user == null){
            System.out.println("존재하지 않는 유저입니다.");
            return;
        }

        //채널에 멤버 추가
        channel.addMember(memberId);


        Message msg = new Message(memberId,channelId,"default message");
        msg.setStatus(Message.messageStatus.INACTIVE);
        messageRepository.saveMessage(msg);


        //repository에 반영
        if(!channelRepository.updateChannel(channel)){
            System.out.println("채널 업데이트 중 문제가 발생했습니다.");
            return;

        }

        User member = userRepository.getUser(memberId);

        System.out.println(channel.getChannelName() +  " 채널에서 " + member.getNickname() + " 멤버 추가 완료!");



    }

    @Override
    public void removeMember(String channelId, String memberId) {

        Channel channel = channelRepository.getChannel(channelId);

        //채널 여부 체크
        if(channel == null){
            System.out.println("존재하지 않는 채널입니다.");
            return;
        }


        //채널 멤버인지 체크
        List<String> channelsUserIdList = channel.getMembers();


        if(!channelsUserIdList.contains(memberId)){
            System.out.println("해당 유저가 채널에 없습니다.");
            return;
        }


        //TODO : 비밀번호 체크 로직 넣기. 인터페이스 및 이전 코드들 약간씩 수정 필요. owner or member 둘중 아무나 맞으면 통과

        channel.removeMember(memberId);

        //삭제한 멤버가 채널장이었을때
        if(channel.getOwnerId().equals(memberId)){


            //채널이 비었으면 삭제
            if(channel.getMembers().isEmpty()){

                System.out.println(channel.getChannelName()+ " 채널의 멤버가 0명이므로 삭제됩니다. ");
                deleteChannel(channelId);
                return;
            }
            //그렇지 않다면 리스트의 첫번째 멤버를 채널장으로 지정
            else{

                List<String> newMembers = channel.getMembers();
                String newOwnerId = newMembers.get(0);

                updateChannelOwner(channelId,newOwnerId);

            }


        }

        if(!channelRepository.updateChannel(channel)){
            System.out.println("채널 업데이트 중 문제가 발생했습니다.");
            return;
        }


        User member = userRepository.getUser(memberId);

        System.out.println(channel.getChannelName() +  " 채널에서 " + member.getNickname() + " 멤버 삭제 완료!");


    }

    @Override
    public void deleteChannel(String channelId) {

        Channel channel = channelRepository.getChannel(channelId);

        //채널 여부 체크
        if(channel == null){
            System.out.println("존재하지 않는 채널입니다.");
            return;
        }

        //TODO : 비밀번호 체크 로직 넣기. 인터페이스 및 이전 코드들 약간씩 수정 필요



        if(!channelRepository.deleteChannel(channelId)){
            System.out.println("채널 삭제중 문제가 발생했습니다.");
            return;
        }

        //채널 삭제시 메시지 전부 삭제
        messageRepository.channelsMessagedelete(channelId);

        //메시지 출력
        System.out.println(channel.getChannelName() + " 채널 삭제 완료!");

    }



    //BasicService 에서는 사용 안함

    @Override
    public boolean isExistChannel(String channelId) {
        //채널 가져오기
        Channel channel = channelRepository.getChannel(channelId);
        return channel != null;

    }

    @Override
    public boolean isChannelsMember(String channelId, String memberId) {
        //채널 가져오기
        Channel channel = channelRepository.getChannel(channelId);
        //채널이 없거나 멤버가 없으면 false
        return channel != null && channel.getMembers().contains(memberId);
    }
}
