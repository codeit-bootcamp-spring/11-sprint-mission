package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.MutableBaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends MutableBaseEntity {

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    public Notification(User receiver, String title, String content) {
        this.id = UUID.randomUUID();
        this.receiver = receiver;
        this.title = title;
        this.content = content;
    }
}
