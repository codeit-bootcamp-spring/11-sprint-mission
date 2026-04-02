package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.userdto.*;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.entity.UserStatus;
import com.sprint.mission.discodeit.exception.service.DiffPasswordException;
import com.sprint.mission.discodeit.exception.service.DupEmailException;
import com.sprint.mission.discodeit.exception.service.DupNameException;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class BasicUserService implements UserService {

    private final UserRepository userRepository;
    private final UserStatusRepository userStatusRepository;
    private final BinaryContentRepository binaryContentRepository;





    @Override
    public UserInfoDto create(CreateUserDto createUserDTO) {



        //유저 생성
        User user = new User(
                createUserDTO.nickname(),
                createUserDTO.email(),
                createUserDTO.password(),
                null
        );
        //프로필 생성
        BinaryContent content = null;
        if(createUserDTO.binaryFile() != null && !createUserDTO.binaryFile().isEmpty()) {



            try {
                content = new BinaryContent(
                        user.getId(),
                        createUserDTO.binaryFile().getOriginalFilename(),
                        createUserDTO.binaryFile().getContentType(),
                        createUserDTO.binaryFile().getBytes()

                );
            }
            catch (Exception e){
                throw new RuntimeException(e);
            }

            content = binaryContentRepository.saveBinaryContent(content);
            user.updateProfileImage(content.getId(), createUserDTO.password());

        }

        // 닉네임 체크
        if(userRepository.isExistUserByNickname(createUserDTO.nickname())){
            throw new DupNameException();
        }

        //이메일 체크
        if(userRepository.isExistUserByEmail(createUserDTO.email())){
           throw new DupEmailException();
        }


        //유저 스테이터스 중복 체크
        if(userStatusRepository.isExistUserStatus(user.getId())){
            throw new ArithmeticException("이미 존재하는 유저 상태입니다.");
        }

        //유저 저장
        userRepository.saveUser(user);

        //유저 상태 저장
        userStatusRepository.saveUserStatus(new UserStatus(user.getId()));

        //프로필 저장
        if(content != null)
            binaryContentRepository.saveBinaryContent(content);

        return userToInfoDto(user);
    }

    @Override
    public UserInfoDto find(UUID userId) {

        //유저 가져오기
        User user = userRepository.getUser(userId).orElseThrow();
        return userToInfoDto(user);

    }

    @Override
    public List<UserDto> findAll() {

        //유저 리스트 가져오기
        return userRepository.getAllUser().stream()
                .map(this::userToDto)
                .toList();

    }

    @Override
    public UserInfoDto updateUser(UpdateUserDto updateUserDto) {

        //유저 가져오기
        User user = userRepository.getUser(updateUserDto.userId()).orElseThrow();

        //기존 닉네임과 다르면 중복 체크 후 변경
        if(!user.getNickname().equals(updateUserDto.newNickname())){

            if(userRepository.isExistUserByNickname(updateUserDto.newNickname())){
                throw new DupNameException();
            }
            user.updateNickname(updateUserDto.newNickname(),updateUserDto.oldPassword());
        }

        if(!user.getEmail().equals(updateUserDto.newEmail())){
            if(userRepository.isExistUserByEmail(updateUserDto.newEmail())){
                throw new DupEmailException();
            }
            user.updateEmail(updateUserDto.newEmail(),updateUserDto.oldPassword());
        }

        if(!user.checkSamePassword(updateUserDto.oldPassword())){
            throw new DiffPasswordException();
        }
        user.updatePassword(updateUserDto.oldPassword(),updateUserDto.newPassword());

        userRepository.saveUser(user);
        return userToInfoDto(user);

    }







    @Override
    public boolean delete(DeleteUserDto deleteUserDto) {

        // 유저 가져오기
        User user = userRepository.getUser(deleteUserDto.userId()).orElseThrow();


        //비밀번호 체크
        if(!user.checkSamePassword(deleteUserDto.password())){
            throw new DiffPasswordException();
        }

        //삭제
        userRepository.deleteUser(deleteUserDto.userId());

        userStatusRepository.deleteUserStatus(deleteUserDto.userId());

        return true;
    }


    UserDto userToDto(User user){

        BinaryContent content = binaryContentRepository.getProfileContentByUserId(user.getId()).orElse(null);

        UUID profileId = content != null ? content.getId() : null;

        return new UserDto(

                user.getId(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getNickname(),
                user.getEmail(),
                profileId,
                userStatusRepository.getUserStatus(user.getId()).orElseThrow().isOnline() == UserStatus.Status.ONLINE

        );


    }



    //유저 -> infoDto
    UserInfoDto userToInfoDto(User user){


        BinaryContent content = binaryContentRepository.getProfileContentByUserId(user.getId()).orElse(null);
        UUID profileId = content != null ? content.getId() : null;


        return new UserInfoDto(

                user.getId(),
                user.getNickname(),
                user.getEmail(),
                profileId,
                userStatusRepository.getUserStatus(user.getId()).orElseThrow()

        );
    }


}


