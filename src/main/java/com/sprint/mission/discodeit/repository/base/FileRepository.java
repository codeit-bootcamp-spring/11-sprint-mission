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

    protected final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    protected final Lock readLock = rwLock.readLock();
    protected final Lock writeLock = rwLock.writeLock();

    protected final String filePath;

    protected final Map<UUID, T> dataMap = new HashMap<>();

    protected FileRepository(String filePath) {
        this.filePath = filePath;
        load();
    }

    protected void postLoad() {}
    protected void postSave(T newEntity, T oldEntity) {}
    protected void postDelete(T entity) {}

    private void load() {
        File file = new File(this.filePath);

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
                this.dataMap.putAll((Map<UUID, T>) object);
            }
            
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new BusinessException(ErrorCode.FILE_DATA_CORRUPTED);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_IO_ERROR);
        }
    }

    private void saveToFile() {
        File file = new File(this.filePath);

        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new BusinessException(ErrorCode.FILE_IO_ERROR);
            }
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
            T copyEntity = (T) entity.copy();

            T oldEntity = dataMap.get(copyEntity.getId());

            dataMap.put(copyEntity.getId(), copyEntity);
            saveToFile();

            postSave(copyEntity, oldEntity);
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
                postDelete(removed);
            }
        } finally {
            writeLock.unlock();
        }
    }
}
