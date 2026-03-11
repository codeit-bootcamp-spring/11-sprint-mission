package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Domain.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.io.*;
import java.util.*;

public class FileChannelRepository implements ChannelRepository {
    private Map<UUID, Channel> data ;   // 실제 데이터 삭제되는 곳
    private Map<UUID, Channel> data_at; // 데이터 삭제시에 바로 전 데이터모습을 복사?

    // TODO
    // 저장(saveToFile), 불러오기 (loadFromFile) 구현
    private void saveToFile(){
        File change = new File("Channel.ser");
        File temp = new File("Channel.ser.temp");

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(temp))) {
            oos.writeObject(data);  // temp에 임시로 저장하고
            temp.renameTo(change);  // 성공하면 기존 파일 대체
        } catch (IOException e) {
            temp.delete();  // 실패하면 temp 파일 삭제
            e.printStackTrace();
        }
    }
    private void loadFromFile(){
        File file = new File("Channel.ser");
        if (!file.exists()) return;  // 파일 없으면 그냥 넘어가기

        try (FileInputStream fis = new FileInputStream("Channel.ser");
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            this.data = (Map<UUID, Channel>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    public FileChannelRepository() {
        this.data = new HashMap<>();
        loadFromFile();
    }

    @Override
    public UUID create(Channel channel) {
        data.put(channel.getId(), channel);
        saveToFile();
        return channel.getId();
    }

    @Override
    public Channel read(UUID id) { return data.get(id); }

    @Override
    public List<Channel> readAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public void delete(UUID id) {
        data_at =new HashMap<>();   // 삭제직전에 at으로 복사하기
        data_at.put(id, data.get(id));
        data.remove(id);
        saveToFile();
    }

    @Override
    public void restore(UUID id) {  // 복구 메커니즘 추가, 삭제 직전에 at으로 복사된걸 다시 가져오기
        if (data_at == null || data_at.get(id) == null) {
            throw new IllegalArgumentException("복구할 데이터가 없습니다.");
        }
        data.put(id, data_at.get(id));
        data_at.remove(id);
        saveToFile();
    }
}
