package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.User;

public interface UserService {

    void createUser(String nickname, String password, String userId);
    void readUser(String userId);
    void readAllUser();
    void updateNickname(String userId, String password,String nickname);
    void updatePassword(String userId, String oldPassword, String newPassword);
    void updateStatus(String userId, String password, User.Status status);
    void deleteUser(String userId, String password);





}
