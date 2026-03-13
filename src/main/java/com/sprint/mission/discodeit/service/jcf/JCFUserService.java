//package com.sprint.mission.discodeit.service.jcf;
//
//import com.sprint.mission.discodeit.entity.User;
//import com.sprint.mission.discodeit.repository.UserRepository;
//import com.sprint.mission.discodeit.service.UserService;
//
//import java.util.List;
//import java.util.UUID;
//
//public class JCFUserService implements UserService {
//    private final UserRepository userRepository;
//
//    public JCFUserService(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    @Override
//    public User createUser(String name, String email, String password) {
//        if (userRepository.findByEmail(email).isPresent()) {
//            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
//        }
//        User user = new User(name, email, password);
//        User savedUser = userRepository.save(user);
//        System.out.println(name + "유저가 생성되었습니다. 생성 시간 : " + savedUser.getCreatedAt());
//        return savedUser;
//    }
//
//    @Override
//    public User readUser(UUID id) {
//        return userRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 유저가 존재하지 않습니다.."));
//    }
//
//    @Override
//    public List<User> allReadUser() {
//        return userRepository.findAll();
//    }
//
//    @Override
//    public void deleteUser(UUID id, String password) {
//        User user = userRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));
//        if(!user.getPassword().equals(password)) {
//            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
//        }
//
//        userRepository.delete(user);
//    }
//
//    @Override
//    public void updateUser(UUID id, String name, String email, String password) {
//        User user = userRepository.findById(id)
//                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));
//
//        user.update(name, email, password);
//
//        userRepository.save(user);
//    }
//}
