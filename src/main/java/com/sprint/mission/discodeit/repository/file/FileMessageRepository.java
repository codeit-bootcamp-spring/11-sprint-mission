package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Domain.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.*;

@Repository
public class FileMessageRepository implements MessageRepository {
    private Map<UUID, Message> data;
    private Map<UUID, Message> data_at;

    // TODO
    // 저장(saveToFile), 불러오기 (loadFromFile) 구현
    private void saveToFile(){
        File change = new File("Message.ser");
        File temp = new File("Message.ser.temp");

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(temp))) {
            oos.writeObject(data);  // temp에 임시로 저장하고
            temp.renameTo(change);  // 성공하면 기존 파일 대체
        } catch (IOException e) {
            temp.delete();  // 실패하면 temp 파일 삭제
            e.printStackTrace();
        }
    }
    private void loadFromFile(){
        File file = new File("Message.ser");
        if (!file.exists()) {
            return;
        }
        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            this.data = (Map<UUID, Message>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public FileMessageRepository() {
        this.data = new HashMap<>();
        loadFromFile();
    }

    @Override
    public UUID create(Message message) {
        data.put(message.getId(), message);
        saveToFile();
        return message.getId();
    }

    @Override
    public Message read(UUID id) {
        return data.get(id);
    }   // key인 id로 메세지 내용 읽기
    // 여기서 메세지는 보낸사람, 받는 사람 포함임

    @Override
    public List<Message> readAll(){
        return new ArrayList<>(data.values());
    }   // value값들 list


    @Override
    public void delete(UUID id) {
        data_at = new HashMap<>(); // 삭제 직전에 at으로 복사해놓기
        data_at.put(id, data.get(id));
        data.remove(id);
        saveToFile();
    }

    @Override
    public String toString() {
        return data.toString();
    }

    @Override
        // 복구
    public void restore(UUID id){
        if(data_at == null || data_at.get(id) == null){
            throw new IllegalArgumentException("복구할 데이터가 없습니다.");
        }
        data.put(id, data_at.get(id));
        data_at.remove(id);
        saveToFile();
    }
}
