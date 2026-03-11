package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Domain.User;
import com.sprint.mission.discodeit.repository.UserRepository;

import java.io.*;
import java.util.*;

public class FileUserRepository implements UserRepository {
    private Map<UUID, User> data;   // 원래는 private final map으로 저장 > 어차피 바뀌니까 final빼기
    private Map<UUID, User> data_at;

    // TODO
    // 저장(saveToFile), 불러오기 (loadFromFile) 구현
    private void saveToFile(){
        File change = new File("users.ser");
        File temp = new File("users.ser.temp");

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(temp))) {
            oos.writeObject(data);  // temp에 임시로 저장하고
            temp.renameTo(change);  // 성공하면 기존 파일 대체
        } catch (IOException e) {
            temp.delete();  // 실패하면 temp 파일 삭제
            e.printStackTrace();
        }
    }
    private void loadFromFile(){
        File file = new File("users.ser");
        if (!file.exists()) return;  // 파일 없으면 그냥 넘어가기

        try (FileInputStream fis = new FileInputStream("users.ser");
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            this.data = (Map<UUID, User>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public FileUserRepository(){
        this.data = new HashMap<>();
        loadFromFile(); // 기존 데이터 불러오게 하기
    }

    @Override
    public UUID create(User user) {
        data.put(user.getId(), user);
        saveToFile();   // 생성 후 파일에 저장
        return user.getId();
    }

    @Override
    public User read(UUID id) {
        return data.get(id);
    }       // 단건조회(key값인 id 넣기)

    @Override
    public List<User> readAll(){
        return new ArrayList<>(data.values());
    }   // 싹다 조회. 리스트로


    @Override
    public void delete(UUID id) {
        data_at = new HashMap<>();
        data_at.put(id, data.get(id));
        data.remove(id);
        saveToFile();   // 삭제 후 파일에 저장
    }   // 삭제(key 값인 id값이 필요함)

    @Override
    public void restore(UUID id) {
        if(data_at == null || data_at.get(id) == null){
            throw new IllegalArgumentException("복구할 데이터가 없습니다.");
        }
        data.put(id, data_at.get(id));
        data_at.remove(id);
        saveToFile();
    }

    @Override
    public String toString() {
        return data.toString();
    }   // 문자열 반환
}
