package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "channels")
@NoArgsConstructor
public class Channel extends BaseUpdatableEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private ChannelType channelType;

    @Column(length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    public Channel(ChannelType channelType, String name, String description) {
        super();
        this.channelType = channelType;
        this.name = name;
        this.description = description;
    }

    public void update(String name, String description) {
        this.name = name;
        this.description = description;
    }

}
