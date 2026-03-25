package com.sprint.mission.discodeit.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ReadStatus implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final UUID channelId;
    private final UUID userId;
    private Instant lastReadAt;

    public void update(Instant lastReadAt) {
        if(lastReadAt == null) {
            throw new IllegalArgumentException("업데이트할 시각이 존재하지 않습니다");
        }

        this.lastReadAt = lastReadAt;

        System.out.println("상태변경 : " + this.id + "의 시각이 " + lastReadAt + "으로 변경되었습니다.");
    }
}
