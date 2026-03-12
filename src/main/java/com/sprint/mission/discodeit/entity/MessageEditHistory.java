package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.time.Instant;

@Getter
public class MessageEditHistory extends BaseEntity {
    private static final long serialVersionUID = 1L;
    private String previousContent;
    private Instant editedAt;

    public MessageEditHistory(String previousContent) {
        super();
        this.previousContent = previousContent;
        this.editedAt = Instant.now();
    }

}
