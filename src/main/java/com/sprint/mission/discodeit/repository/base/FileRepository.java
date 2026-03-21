package com.sprint.mission.discodeit.repository.base;

import com.sprint.mission.discodeit.entity.base.ImmutableBaseEntity;
import com.sprint.mission.discodeit.exception.BusinessException;
import com.sprint.mission.discodeit.exception.ErrorCode;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class FileRepository<T extends ImmutableBaseEntity> {

    // 이 부분이 좀...? ReentrantReadWriteLock 공부
    // - 간략한건 writeLock이 뒤로 밀릴 정도로 요청이 많이와도 자바에서 관리해준다는 사실 정도
    protected final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    protected final Lock readLock = rwLock.readLock();
    protected final Lock writeLock = rwLock.writeLock();

    protected final String filePath;

    protected final Map<UUID, T> dataMap = new HashMap<>();
    // Q: 락을 걸어주면 HashMap을 사용해도 Thread-Safe 한 것 아닌가?

    protected FileRepository(String filePath) {
        this.filePath = filePath;
        load(); // 생성 시 파일에서 읽어오기
    }

    // Hook 메서드: 인덱스 저장 용도
    protected abstract void postLoad();
    protected void postSave(T newEntity, T oldEntity) {}
    protected void postDelete(T entity) {}

    private void load() {
        File file = new File(filePath);

        // 현재 파일이 없는 경우
        if (!file.exists()) {
            return;
        }

        try(
                FileInputStream fis = new FileInputStream(file);
                BufferedInputStream bis = new BufferedInputStream(fis);
                ObjectInputStream ois = new ObjectInputStream(bis);
        ) {
            Object object = ois.readObject();
            if (object instanceof Map) {
                this.dataMap.clear();
                this.dataMap.putAll((Map<UUID, T>)object);
            }
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new BusinessException(ErrorCode.FILE_DATA_CORRUPTED);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_IO_ERROR);
        }
    }

    private void saveToFile() {
        File file = new File(filePath);

        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                ObjectOutputStream oos = new ObjectOutputStream(bos)
        ) {
            oos.writeObject(dataMap);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_IO_ERROR);
        }
    }

    @SuppressWarnings("unchecked")
    public T save(T entity) {
        writeLock.lock();
        try {
            T copyEntity = (T) entity.copy(); // 원본 객체 훼손 위험성이 존재함!!

            T oldEntity = dataMap.get(copyEntity.getId());

            dataMap.put(copyEntity.getId(), copyEntity);
            saveToFile();

            postSave(copyEntity, oldEntity); // 인덱스 갱신
            return copyEntity;
        } finally {
            writeLock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public Optional<T> findById(UUID id) {
        readLock.lock();
        try {
            T entity = dataMap.get(id);
            return entity != null ? Optional.of((T) entity.copy()) : Optional.empty();
        } finally {
            readLock.unlock();
        }
    }

    @SuppressWarnings("unchecked")
    public List<T> findAll() {
        readLock.lock();
        try {
            List<T> list = new ArrayList<>();
            for (T entity : dataMap.values()) {
                T copied = (T) entity.copy();
                list.add(copied);
            }
            return Collections.unmodifiableList(list);
        } finally {
            readLock.unlock();
        }
    }

    public void deleteById(UUID id) {
        writeLock.lock();
        try {
            T removed = dataMap.remove(id);
            if (removed != null) {
                saveToFile();
                postDelete(removed); // 인덱스 갱신
            }
        } finally {
            writeLock.unlock();
        }
    }
}
