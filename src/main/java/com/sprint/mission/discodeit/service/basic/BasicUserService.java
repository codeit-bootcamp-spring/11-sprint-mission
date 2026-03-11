package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BasicUserService implements UserService {

    UserRepository userRepository;
    ChannelRepository channelRepository;
    MessageRepository messageRepository;

    public BasicUserService(UserRepository userRepository, ChannelRepository channelRepository, MessageRepository messageRepository) {

        this.userRepository = userRepository;
        this.channelRepository = channelRepository;
        this.messageRepository = messageRepository;
    }


    @Override
    public void createUser(String nickname, String password, String userId) {

        //유저 생성
        User user = new User(userId, password, nickname);

        //리포지토리에 저장. (이미 있는 아이디라면 false 반환)
        if(!userRepository.saveUser(user)){

            System.out.println("이미 존재하는 유저 아이디 입니다.");
            return;
        }

        //출력
        System.out.println(user.getNickname()+ " 님 생성 완료!");



    }

    @Override
    public void readUser(String userId) {

        //유저 가져오기
        User user = userRepository.getUser(userId);

        //없는 유저라면
        if(user == null){
            System.out.println("존재하지 않는 유저입니다.");
            return;
        }

        //출력
        System.out.println(user);



    }

    @Override
    public void readAllUser() {

        //유저 리스트 가져오기
        List<User> users = userRepository.getAllUser();


        //리스트를 가져오는데 문제가 있는경우
        if(users == null){
            System.out.println("유저 리스트를 불러오는데 문제가 발생했습니다.");
            return;
        }


        //stream으로 전체 출력
        users.stream()
                .sorted(User::compareTo)
                .forEach(System.out::println);




    }

    @Override
    public void updateNickname(String userId, String password, String nickname) {

        //유저 가져오기
        User user = userRepository.getUser(userId);

        // 잘못된 유저 아이디 체크
        if(user == null){
            System.out.println("존재하지 않는 유저 아이디입니다.");
            return;
        }

        // 닉네임 업데이트
        if(!user.updateNickname(nickname,password)){
            //비밀번호 불일치
            System.out.println("패스워드가 일치하지 않습니다.");
            return;

        }

        //repository에 반영
        if(!userRepository.updateUser(user)){
            System.out.println("유저 업데이트 중 이상이 발생했습니다.");
            return;
        }

        System.out.println( user.getUserId() + " 님 닉네임 " + user.getNickname()  + " 으로 변경 완료!" );




    }

    @Override
    public void updatePassword(String userId, String oldPassword, String newPassword) {

        User user = userRepository.getUser(userId);

        // 잘못된 유저 아이디 체크
        if(user == null){
            System.out.println("존재하지 않는 유저 아이디입니다.");
            return;
        }

        // 비밀번호 업데이트
        if(!user.updatePassword(oldPassword,newPassword)){
            //비밀번호 불일치
            System.out.println("패스워드가 일치하지 않습니다.");
            return;

        }

        //repository에 반영
        if(!userRepository.updateUser(user)){
            System.out.println("유저 업데이트 중 이상이 발생했습니다.");
            return;
        }

        System.out.println("유저 비밀번호 업데이트 완료!");

    }

    @Override
    public void updateStatus(String userId, String password, User.Status status) {

        User user = userRepository.getUser(userId);

        // 잘못된 유저 아이디 체크
        if(user == null){
            System.out.println("존재하지 않는 유저 아이디입니다.");
            return;
        }

        // 상태 업데이트
        if(!user.updateStatus(status,password)){
            //비밀번호 불일치
            System.out.println("패스워드가 일치하지 않습니다.");
            return;

        }

        //repository에 반영
        if(!userRepository.updateUser(user)){
            System.out.println("유저 업데이트 중 이상이 발생했습니다.");
            return;

        }


        System.out.println("유저 상태 변경 완료 -> " + user.getStatus());



    }

    @Override
    public void deleteUser(String userId, String password) {



        // 유저 가져오기
        User user = userRepository.getUser(userId);


        // 잘못된 유저 아이디 체크
        if(user == null){
            System.out.println("존재하지 않는 유저 아이디입니다.");
            return;
        }

        //잘못된 비밀번호 체크
        if(!user.checkSamePassword(password)){
            System.out.println("패스워드가 일치하지 않습니다.");
            return;

        }







        //삭제
        if(!userRepository.deleteUser(userId)){
            System.out.println("삭제 도중 이상이 발생했습니다.");
            return;
        }



        List<Channel> channels = new ArrayList<>();

        //모든 디폴트 메시지를 확인하면서
        user.getDefaultMessages().stream()
                        .map(message -> channelRepository.getChannel(message.getChannelId()))
                                .filter(Objects::nonNull)
                                        .forEach(channel ->{

                                            //디폴트 메시지가 있는 채널에서 유저 삭제
                                            channel.getMembers().remove(userId);

                                            //채널 주인인 경우 처리
                                            if(channel.getOwnerId().equals(userId)){
                                                //채널에 아무도 없게 되었다면 채널 삭제
                                                if(channel.getMembers().isEmpty()){
                                                    channelRepository.deleteChannel(channel.getChannelId());
                                                    return;
                                                }
                                                //채널 남은 사람한테 채널장 넘겨주기
                                                else{
                                                    channel.updateOwner(channel.getMembers().get(0));
                                                }
                                            }
                                            //리포지토리에 반영
                                            channelRepository.updateChannel(channel);
                                        });

        //출력
        System.out.println(user.getNickname() + " 님 삭제 완료!");

    }


}


