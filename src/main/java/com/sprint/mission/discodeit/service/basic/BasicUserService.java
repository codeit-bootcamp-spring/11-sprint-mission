package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class BasicUserService implements UserService {
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String PHONE_REGEX = "^\\d{3}-\\d{3}-\\d{4}$";

    @Override
    public User createUser(String nickname, String username, String email, String password, String phoneNumber) {
        if (nickname == null || nickname.isBlank()) throw new IllegalArgumentException("nickname is required. ❌");

        if (username == null || username.isBlank()) throw new IllegalArgumentException("username is required. ❌");
        if (this.existUserByUsername(username)) throw new IllegalArgumentException("username cannot be duplicated. ❌");

        if (email == null || email.isBlank()) throw new IllegalArgumentException("email is required. ❌");
        if (!email.matches(EMAIL_REGEX)) throw new IllegalArgumentException("email format is invalid. ❌");
        if (this.existUserByEmail(email)) throw new IllegalArgumentException("email cannot be duplicated. ❌");

        if (password == null || password.isBlank()) throw new IllegalArgumentException("password is required. ❌");
        if (password.length() < 8) throw new IllegalArgumentException("password length should be at least 8 characters. ❌");

        if (phoneNumber == null || phoneNumber.isBlank()) throw new IllegalArgumentException("phone number is required. ❌");
        if (!phoneNumber.matches(PHONE_REGEX)) throw new IllegalArgumentException("phone number format is invalid. ❌");

        User user = new User(nickname, username, email, password, phoneNumber);
        this.userRepository.save(user);

        log.info("{} has been created successfully. ✅ [ID: {}]", nickname, user.getId());
        return user;
    }

    @Override
    public User getUserById(UUID id) {
        return this.userRepository.findById(id);
    }

    @Override
    public boolean existUserByUsername(String username) {
        return this.userRepository.existByUsername(username);
    }

    @Override
    public boolean existUserByEmail(String email) {
        return this.userRepository.existByEmail(email);
    }

    @Override
    public List<User> getAllUsers() {
        return this.userRepository.findAll();
    }

    @Override
    public User updateUser(UUID id, String nickname, String username, String email, String password, String phoneNumber) {
        User user = this.getUserById(id);

        if (nickname != null && !nickname.isBlank()) user.updateNickname(nickname);
        if (username != null && !username.isBlank()) {
            if (this.existUserByUsername(username)) throw new IllegalArgumentException("username cannot be duplicated. ❌");
            user.updateUsername(username);
        }
        if (email != null && !email.isBlank()) {
            if (!email.matches(EMAIL_REGEX)) throw new IllegalArgumentException("email format is invalid. ❌");
            if (this.existUserByEmail(email)) throw new IllegalArgumentException("email cannot be duplicated. ❌");
            user.updateEmail(email);
        }
        if (password != null && !password.isBlank()) {
            if (password.length() < 8) throw new IllegalArgumentException("password length should be at least 8 characters. ❌");
            user.updatePassword(password);
        }
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            if (!phoneNumber.matches(PHONE_REGEX)) throw new IllegalArgumentException("phone number format is invalid. ❌");
            user.updatePhoneNumber(phoneNumber);
        }

        this.userRepository.save(user);

        log.info("{} has been updated successfully. ✅ [ID: {}]", user.getNickname(), id);
        return user;
    }

    @Override
    public void deleteUser(UUID id) {
        User user = this.getUserById(id);

        user.getChannels().
                forEach(channel -> {
                    channel.getParticipants().removeIf(participant -> participant.getId().equals(user.getId()));
                    this.channelRepository.save(channel);
                });

        this.userRepository.delete(user);

        log.info("{} has been deleted successfully and left from all channels. ✅ [ID: {}]", user.getNickname(), id);
    }
}
