package com.sprint.mission.dicordeit.service;

import com.sprint.mission.dicordeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface UserService {

    User createUser(String username, String password, String email);

    User readUser(UUID id);

    List<User> readAll();

    User update(UUID id, String password, String email);

    void delete(UUID id);
}