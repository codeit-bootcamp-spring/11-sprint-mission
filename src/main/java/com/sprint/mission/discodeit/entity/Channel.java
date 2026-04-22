package com.sprint.mission.discodeit.entity;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import com.sprint.mission.discodeit.entity.base.MutableBaseEntity;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "channels")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Channel extends MutableBaseEntity{

    @Column(length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChannelType type;

    public Channel(String name, String description) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.description = description;
        this.type = ChannelType.PUBLIC;
    }

    public Channel(ChannelType type) {
        this.id = UUID.randomUUID();
        this.type = type;
        this.name = null;
        this.description = null;
    }

    public  static Channel createPrivateChannel() {
        return new Channel(ChannelType.PRIVATE);
    }

    public void update(String name, String description) {
        if (type == ChannelType.PRIVATE) {
            throw new IllegalArgumentException("PRIVATE 채널은 수정할 수 없습니다.");
        }

        this.name = name;
        this.description = description;
    }
}
