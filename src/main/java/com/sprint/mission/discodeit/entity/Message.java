package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "messages")
@NoArgsConstructor
public class Message extends BaseUpdatableEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "channel_id", nullable = false)
  private Channel channel;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id")
  private User author;

  @Column(columnDefinition = "text")
  private String content;

  @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  @JoinTable(name = "message_attachments", joinColumns = @JoinColumn(name = "message_id"), inverseJoinColumns = @JoinColumn(name = "attachment_id"))
  private List<BinaryContent> attachments = new ArrayList<>();

  public Message(Channel channel, User author, String content) {
    this.channel = channel;
    this.author = author;
    this.content = content;
  }

  public void addAttachment(BinaryContent binaryContentId) {
    if (binaryContentId != null) {
      this.attachments.add(binaryContentId);
    }
  }

  // 필드를 수정하는 update 함수
  public void update(String newMessage) {
    validateMessage(newMessage);
    this.content = newMessage;
  }

  private void validateMessage(String message) {
    if (message == null || message.trim().isEmpty()) {
      throw new IllegalArgumentException("메시지 내용은 비어 있을 수 없습니다.");
    }
  }


  @Override
  public String toString() {
    return "메세지 : " + content;
  }
}
