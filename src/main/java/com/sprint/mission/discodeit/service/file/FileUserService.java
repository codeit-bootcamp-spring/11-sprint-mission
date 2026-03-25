//package com.sprint.mission.discodeit.service.file;
//
//import com.sprint.mission.discodeit.entity.User;
//import com.sprint.mission.discodeit.repository.UserRepository;
//import com.sprint.mission.discodeit.service.UserService;
//
//import java.util.UUID;
//
//public class FileUserService implements UserService {
//    // 기존 JCF Service의 경우 프로그램 종료 시 메모리에서만 존재하기 때문에 프로그램 종료 시 동시에 데이터가 사라진다.
//    // FileSystem을 통해 데이터를 남겨놓기
//    // 직렬화 : Java 객체 -> 바이트 배열 -> 파일
//    // 역직렬화 : 파일 -> 바이트 배열 -> Java 객체
//
//    // 직렬화(Save -> Create를 저장, Update를 저장, Delete를 저장), 역직렬화(Load -> 불러오기)
//
//    // User들을 담을 Map 생성
////    private final Map<UUID, User> users = new HashMap<>(); // 저장소
//
//    // 저장 메서드 save(직렬화)
////    public void save() {
////        try (FileOutputStream fos = new FileOutputStream("users.ser");
////        ObjectOutputStream oos = new ObjectOutputStream(fos);
////        ) {
////            oos.writeObject(users);
////        }
////            catch (IOException e) {
////                e.printStackTrace();
////        }
////
////    }
//
//    // 불러오기 메서드 load(역직렬화)
////    public void load() {
////        try (FileInputStream fis = new FileInputStream("users.ser");
////        ObjectInputStream ois = new ObjectInputStream(fis)) {
////            Map<UUID, User> loadUsers = (Map<UUID, User>) ois.readObject();
////            users.clear(); // 한 번 비우고
////            users.putAll(loadUsers); // 불러온다.(기존에 있던 데이터까지 같이 로드될 수 있기 때문에)
////        } catch (IOException | ClassNotFoundException e) {
////            e.printStackTrace();
////        }
////
////    }
//
//    // 닉네임으로 UUID 호출
////    public UUID findIdByNickname(String nickname) {
////        userRepository.load();
////        for (Map.Entry<UUID, User> user : users.entrySet()) {
////            if (user.getValue().getNickname().equals(nickname)) {
////                return user.getKey();
////            }
////        }
////        return null;
////    }
//
//    private final UserRepository userRepository;
//
//    public FileUserService(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    // Create
//    @Override
//    public User create(String name, String email, String password) {
//        // 저장 로직 분리 전
////        users.put(user.getId(), user);
////        save();
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
////        load();
////
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
////        load();
////
////        // NPE 방지
////        if (!users.containsKey(id)) { System.out.println("해당 유저가 존재하지 않습니다."); }
////        else {
////            User user = users.get(id);
////            System.out.println("수정 전 유저 이름 : " + user.getName());
////            user.updateName(newName);
////            System.out.println("수정 후 유저 이름 : " + user.getName());
////            save();
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
////        load();
////
////        // NPE 방지
////        if (!users.containsKey(id)) { System.out.println("해당 유저가 존재하지 않습니다."); }
////        else {
////            User user = users.get(id);
////            System.out.println("수정 전 유저 별명 : " + user.getNickname());
////            user.updateNickname(newNickname);
////            System.out.println("수정 후 유저 별명 : " + user.getNickname());
////            save();
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
////        load();
////
////        // NPE 방지
////        if (!users.containsKey(id)) { System.out.println("해당 유저가 존재하지 않습니다."); }
////        else {
////            User user = users.get(id);
////            System.out.println("수정 전 유저 이메일 : " + user.getEmail());
////            user.updateEmail(newEmail);
////            System.out.println("수정 후 유저 이메일 : " + user.getEmail());
////            save();
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
////        load();
////
////        // NPE 방지
////        if (!users.containsKey(id)) { System.out.println("해당 유저가 존재하지 않습니다."); }
////        else {
////            User user = users.get(id);
////            System.out.println("수정 전 유저 전화번호 : " + user.getPhoneNumber());
////            user.updatePhoneNumber(newPhoneNumber);
////            System.out.println("수정 후 유저 전화번호 : " + user.getPhoneNumber());
////            save();
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
////        load();
////
////        // NPE 방지
////        if (!users.containsKey(id)) { System.out.println("해당 유저가 존재하지 않습니다."); }
////        else {
////            User user = users.get(id);
////            System.out.println("수정 전 유저 프로필 이미지 : " + user.getProfileImageURL());
////            user.updateProfileImageURL(newProfileImageURL);
////            System.out.println("수정 후 유저 프로필 이미지 : " + user.getProfileImageURL());
////            save();
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
////        load();
////
////        // NPE 방지
////        if (!users.containsKey(id)) { System.out.println("해당 유저가 존재하지 않습니다."); }
////        else {
////            User user = users.get(id);
////            System.out.println("수정 전 유저 상태 : " + user.getUserStatus());
////            user.updateStatus(newStatus);
////            System.out.println("수정 후 유저 상태 : " + user.getUserStatus());
////            save();
////        }
////        System.out.println();
//
//        // 저장 로직 분리 후
//        // 유저가 존재하는지 확인
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
////        load();
////        if (!users.containsKey(id)) { System.out.println("해당 유저는 존재하지 않습니다."); }
////        else {
////            User user = users.get(id);
////            users.remove(id);
////            System.out.println("유저 " + user.getNickname() + "이(가) 삭제되었습니다.");
////            save();
////        }
////        System.out.println();
////    }
//
//        // 저장 로직 분리 후
//        User user = userRepository.findById(id);
//        userRepository.delete(id);
//        System.out.println("유저 " + user.getNickname() + "이(가) 삭제되었습니다.");
//        System.out.println();
//    }
//}
