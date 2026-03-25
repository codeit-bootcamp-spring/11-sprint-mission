//package com.sprint.mission.discodeit.service.jcf;
//
//import com.sprint.mission.discodeit.entity.User;
//import com.sprint.mission.discodeit.repository.UserRepository;
//import com.sprint.mission.discodeit.service.UserService;
//
//import java.util.UUID;
//
//public class JCFUserService implements UserService {
//
//    private final UserRepository userRepository;
//
//    public JCFUserService(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
////    private final Map<UUID, User> users = new HashMap<>(); // 저장소
//    // 저장로직 Map 함수
//    // .put : Map에 유저 UUID랑 유저를 넣음 -> insert
//    // .containsKey : 해당 UUID를 Map이 가지고 있는지 확인 -> isExistsUser
//    // .get : 해당 UUID를 가진 user 반환, -> findUser
//    // .remove : 해당 UUID를 가진 user 삭제, -> delete
//
//    // Create
//    @Override
//    public User create(String name, String email, String password) {
//        // 저장 로직 분리 전
////        users.put(user.getId(), user);
////        System.out.println("유저를 추가하였습니다.");
////        System.out.println();
//
//        // 저장 로직 분리 후
//        User user = User.create(name, email, password);
//        userRepository.insert(user);
//        System.out.println("유저를 추가하였습니다.");
//        System.out.println();
//
//        return user;
//    }
//
//
//    // Read
//    @Override
//    public User readAll(UUID id) {
//        // 저장 로직 분리 전
////        // NPE 방지
////        if (!users.containsKey(id)) { System.out.println("해당 유저가 존재하지 않습니다."); }
////        else {
////            User user = users.get(id);
////            System.out.println("=====유저 정보=====\n" + user); }
////
////        System.out.println();
//
//        // 저장 로직 분리 후
//        User user = userRepository.findById(id);
//        System.out.println("=====유저 정보=====\n" + user);
//        System.out.println();
//
//        return user;
//    }
//
//
//    // Update
//    // 같은 키, 다른 Value를 put 하면 키는 그대로, Value만 갱신된다.
//    @Override
//    public User updateName(UUID id, String newName) {
//        // 저장 로직 분리 전
////        // NPE 방지
////        if (!users.containsKey(id)) { System.out.println("해당 유저가 존재하지 않습니다."); }
////        else {
////            User user = users.get(id);
////            System.out.println("수정 전 유저 이름 : " + user.getName());
////            user.updateName(newName);
////            System.out.println("수정 후 유저 이름 : " + user.getName());
////
////        }
////        System.out.println();
//
//        // 저장 로직 분리 후
//        User user = userRepository.findById(id);
//        System.out.println("수정 전 유저 이름 : " + user.getName());
//        user.updateName(newName);
//        System.out.println("수정 후 유저 이름 : " + user.getName());
//        userRepository.update(user);
//        System.out.println();
//
//        return user;
//    }
//
//    @Override
//    public User updateNickname(UUID id, String newNickname) {
//        // 저장 로직 분리 전
////        // NPE 방지
////        if (!users.containsKey(id)) { System.out.println("해당 유저가 존재하지 않습니다."); }
////        else {
////            User user = users.get(id);
////            System.out.println("수정 전 유저 별명 : " + user.getNickname());
////            user.updateNickname(newNickname);
////            System.out.println("수정 후 유저 별명 : " + user.getNickname());
////        }
////        System.out.println();
//
//        // 저장 로직 분리 후
//        User user = userRepository.findById(id);
//        System.out.println("수정 전 유저 별명 : " + user.getNickname());
//        user.updateNickname(newNickname);
//        System.out.println("수정 후 유저 별명 : " + user.getNickname());
//        userRepository.update(user);
//        System.out.println();
//
//        return user;
//    }
//
//    @Override
//    public User updateEmail(UUID id, String newEmail) {
//        // 저장 로직 분리 전
////        // NPE 방지
////        if (!users.containsKey(id)) { System.out.println("해당 유저가 존재하지 않습니다."); }
////        else {
////            User user = users.get(id);
////            System.out.println("수정 전 유저 이메일 : " + user.getEmail());
////            user.updateEmail(newEmail);
////            System.out.println("수정 후 유저 이메일 : " + user.getEmail());
////        }
////        System.out.println();
//
//        // 저장 로직 분리 후
//        User user = userRepository.findById(id);
//        System.out.println("수정 전 유저 이메일 : " + user.getEmail());
//        user.updateEmail(newEmail);
//        System.out.println("수정 후 유저 이메일 : " + user.getEmail());
//        userRepository.update(user);
//        System.out.println();
//
//        return user;
//    }
//
//    @Override
//    public User updatePhoneNumber(UUID id, String newPhoneNumber) {
//        // 저장 로직 분리 전
////        // NPE 방지
////        if (!users.containsKey(id)) { System.out.println("해당 유저가 존재하지 않습니다."); }
////        else {
////            User user = users.get(id);
////            System.out.println("수정 전 유저 전화번호 : " + user.getPhoneNumber());
////            user.updatePhoneNumber(newPhoneNumber);
////            System.out.println("수정 후 유저 전화번호 : " + user.getPhoneNumber());
////        }
////        System.out.println();
//
//        // 저장 로직 분리 후
//        User user = userRepository.findById(id);
//        System.out.println("수정 전 유저 전화번호 : " + user.getPhoneNumber());
//        user.updatePhoneNumber(newPhoneNumber);
//        System.out.println("수정 후 유저 전화번호 : " + user.getPhoneNumber());
//        userRepository.update(user);
//        System.out.println();
//
//        return user;
//    }
//
//    @Override
//    public User updateProfileImageURL(UUID id, String newProfileImageURL) {
//        // 저장 로직 분리 전
////        // NPE 방지
////        if (!users.containsKey(id)) { System.out.println("해당 유저가 존재하지 않습니다."); }
////        else {
////            User user = users.get(id);
////            System.out.println("수정 전 유저 프로필 이미지 : " + user.getProfileImageURL());
////            user.updateProfileImageURL(newProfileImageURL);
////            System.out.println("수정 후 유저 프로필 이미지 : " + user.getProfileImageURL());
////        }
////        System.out.println();
//
//        // 저장 로직 분리 후
//        User user = userRepository.findById(id);
//        System.out.println("수정 전 유저 프로필 이미지 : " + user.getProfileImageURL());
//        user.updateProfileImageURL(newProfileImageURL);
//        System.out.println("수정 후 유저 프로필 이미지 : " + user.getProfileImageURL());
//        userRepository.update(user);
//        System.out.println();
//
//        return user;
//    }
//
//    @Override
//    public User updateStatus(UUID id, User.Status newStatus) {
//        // 저장 로직 분리 전
////        // NPE 방지
////        if (!users.containsKey(id)) { System.out.println("해당 유저가 존재하지 않습니다."); }
////        else {
////            User user = users.get(id);
////            System.out.println("수정 전 유저 상태 : " + user.getUserStatus());
////            user.updateStatus(newStatus);
////            System.out.println("수정 후 유저 상태 : " + user.getUserStatus());
////        }
////        System.out.println();
//
//        // 저장 로직 분리 후
//        User user = userRepository.findById(id);
//        System.out.println("수정 전 유저 상태 : " + user.getStatus());
//        user.updateStatus(newStatus);
//        System.out.println("수정 후 유저 상태 : " + user.getStatus());
//        userRepository.update(user);
//        System.out.println();
//
//        return user;
//    }
//
//    // Delete
//    @Override
//    public void delete(UUID id) {
//        // 저장 로직 분리 전
////        // NPE 방지
////        if (!users.containsKey(id)) { System.out.println("해당 유저는 존재하지 않습니다."); }
////        else {
////            User user = users.get(id);
////            System.out.println("유저 " + user.getNickname() + "이(가) 삭제되었습니다.");
////            users.remove(id);
////        }
////        System.out.println();
//
//        // 저장 로직 분리 후
//        User user = userRepository.findById(id);
//        userRepository.delete(id);
//        System.out.println("유저 " + user.getNickname() + "이(가) 삭제되었습니다.");
//        System.out.println();
//    }
//}
