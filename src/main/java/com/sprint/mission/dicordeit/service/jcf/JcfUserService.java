package com.sprint.mission.dicordeit.service.jcf;

import com.sprint.mission.dicordeit.service.UserService;
import com.sprint.mission.dicordeit.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JcfUserService implements UserService {

    private final List<User> data = new ArrayList<>();

    @Override
    public User createUser(String username, String password, String email) {
        User newUser = new User(username, password, email);

        data.add(newUser);

        return newUser;
    }

    @Override
    public User readUser(UUID id) {
        return data.stream()
                .filter(user -> user.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<User> readAll() {
        return data;
    }


    @Override
    public User update(UUID id, String password, String email) {
        //기존 유저 정보 가져오기 유저 확인하기
        User targetUser = readUser(id);
        if (targetUser != null) {
            //가져온 정보에서 업데이트 비밀번호 이메일
            targetUser.update(password, email);
            return targetUser;
        }
        return null;

    }

    @Override
    public void delete(UUID id) {
        data.remove(readUser(id));

    }
}
