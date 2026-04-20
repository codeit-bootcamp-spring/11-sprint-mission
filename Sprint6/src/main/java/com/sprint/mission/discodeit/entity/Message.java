package com.sprint.mission.discodeit.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "messages") // 복수형 권장 반영
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message extends BaseUpdatableEntity {

  @Column(columnDefinition = "TEXT", nullable = false)
  private String content;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id")
  private User author;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "channel_id")
  private Channel channel;

  // N:M 관계 설정 추가
  @ManyToMany
  @JoinTable(
      name = "message_binary_contents", // 중간 테이블 명
      joinColumns = @JoinColumn(name = "message_id"),
      inverseJoinColumns = @JoinColumn(name = "binary_content_id")
  )
  private List<BinaryContent> attachments = new ArrayList<>();

  @Builder
  public Message(String content, User author, Channel channel, List<BinaryContent> attachments) {
    this.content = content;
    this.author = author;
    this.channel = channel;
    // Builder에서 첨부파일 리스트 초기화 반영
    this.attachments = (attachments != null) ? attachments : new ArrayList<>();
  }

  public void update(String newContent) {
    if (newContent != null) {
      this.content = newContent;
    }
  }
}