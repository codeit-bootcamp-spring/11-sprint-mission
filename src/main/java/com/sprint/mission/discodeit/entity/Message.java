package com.sprint.mission.discodeit.entity;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.Getter;
import java.util.ArrayList;
import com.sprint.mission.discodeit.entity.base.MutableBaseEntity;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message extends MutableBaseEntity{

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    @Column(columnDefinition = "TEXT")
    private String content;

    @OneToMany
    @JoinTable(
            name = "message_attachments",
            joinColumns = @JoinColumn(name = "message_id"),
            inverseJoinColumns = @JoinColumn(name = "attachment_id")

    )
    private List<BinaryContent> attachments = new ArrayList<>();

    public Message(User author, Channel channel, String content) {
        this.id = UUID.randomUUID();
        this.author = author;
        this.channel = channel;
        this.content = content;
        this.attachments = new ArrayList<>();
    }

    public void update(String content) {
        this.content = content;
    }

    public void updateAttachments(List<BinaryContent> attachments) {
        this.attachments = new ArrayList<>(attachments);
    }
}
