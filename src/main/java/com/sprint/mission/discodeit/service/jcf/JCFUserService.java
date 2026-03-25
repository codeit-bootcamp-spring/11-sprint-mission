//package com.sprint.mission.discodeit.service.jcf;
//
//import com.sprint.mission.discodeit.entity.User;
//import com.sprint.mission.discodeit.service.UserService;
//
//import java.util.*;
//
//public class JCFUserService implements UserService{
//    private final Map<UUID, User> data;
//
//    public JCFUserService() {
//        this.data = new HashMap<>();
//    }
//
//    @Override
//    public User create(User user) {
//        data.put(user.getId(), user);
//        return user;
//    }
//
//    @Override
//    public List<User> getAllUser() {
//        return data.values().stream().toList();
//    }
//
//    @Override
//    public User findById(UUID id) {
//        User user = data.get(id);
//        if (user == null) {
//            throw new IllegalArgumentException("[user] 없는 id 입니다.");
//        }
//        return user;
//    }
//
//    @Override
//    public User updateName(UUID id, String name) {
//        User user = findById(id);
//        user.updateName(name);
//        return user;
//    }
//
//    @Override
//    public User updateStatus(UUID id, String status) {
//        User user = findById(id);
//        user.updateStatus(status);
//        return user;
//    }
//
//    @Override
//    public void delete(UUID id) {
//        findById(id);
//        data.remove(id);
//    }
//}
