package com.sprint.mission.discodeit.sse.repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class SseEmitterRepository {

  private final ConcurrentMap<UUID, List<SseEmitter>> data = new ConcurrentHashMap<>();

  public void save(UUID receiverId, SseEmitter sseEmitter) {
    // data 내부에 receiverId가 없을때만 새 List를 만들어서 추가
    // Race condition 방지하기 위해 일반 ArrayList가 아닌 CopyOnWriteArrayList를 사용
    // 쓰기 작업(생성/수정/삭제)가 일어날 때 새 배열에 작업 반영 포함하여 복사시킴(복사 후 기존 배열은 GC에서 삭제)
    data.computeIfAbsent(receiverId, key -> new CopyOnWriteArrayList<>()).add(sseEmitter);
  }

  public List<SseEmitter> findAllByReceiverId(UUID receiverId) {
    // receiverId가 없으면 비어있는 ArrayList를 반환(NPE 방지)
    return data.getOrDefault(receiverId, List.of());
  }

  public Map<UUID, List<SseEmitter>> findAll() {
    return data;
  }

  public void delete(UUID receiverId, SseEmitter sseEmitter) {
    // receiverId에 해당하는 Value를 가져옴
    List<SseEmitter> emitters = data.get(receiverId);

    // Value List가 존재할 때만 Value들 중 조건에 맞는 단건 Value 삭제
    if (emitters != null) {
      emitters.remove(sseEmitter);

      // 더 이상 Value가 없을 경우 Key까지 같이 삭제
      if (emitters.isEmpty()) {
        data.remove(receiverId, emitters);
      }
    }
  }

}
