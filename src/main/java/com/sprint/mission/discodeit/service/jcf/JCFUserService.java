//package com.sprint.mission.discodeit.service.jcf;
//
//import com.sprint.mission.discodeit.entity.User;
//import com.sprint.mission.discodeit.service.UserService;
//
//import java.util.*;
//
//public class JCFUserService implements UserService {
//    private final Map<UUID, User> data;
//
//    public JCFUserService() {
//        this.data = new HashMap<>();
//    }
//
//    @Override
//    public User create(String name, String email, String password) {
//        User user = new User(name, email, password);
//        data.put(user.getId(), user);
//        return user;
//    }
//
//    @Override
//    public Optional<User> findById(UUID id) {
//        return Optional.ofNullable(data.get(id));
//    }
//
//    @Override
//    public List<com.sprint.mission.discodeit.dto.UserDto> findAll() {
//        return data.values().stream().toList();
//    }
//
//    @Override
//    public void update(UUID id, String name) {
//        User user = data.get(id);
//        if(user != null) {
//            user.update(name);
//        }
//    }
//
//    @Override
//    public void delete(UUID id) {
//        data.remove(id);
//    }
//}