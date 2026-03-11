package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.User;

import java.util.List;

public interface UserRepository {

    boolean saveUser(User user);
    User getUser(String userId);
    List<User> getAllUser();
    boolean updateUser(User user);
    boolean deleteUser(String userId);
    boolean isExistUser(String userId);







}
