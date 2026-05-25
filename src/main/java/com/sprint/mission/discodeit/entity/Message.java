package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import java.util.List;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
// 채널별로 폴더를 나누고 그 안에서 시간 순으로 정렬하도록 복합 인덱스를 걸음.
@Table(name = "messages", indexes = {
    @Index(name = "idx_channel_id_created_at", columnList = "channel_id, created_at")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Message extends BaseUpdatableEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id")
  @OnDelete(action = OnDeleteAction.SET_NULL)
  private User author;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "channel_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Channel channel;

  @Column(columnDefinition = "text")
  private String content;

  @BatchSize(size = 100) // message id 100개까지, 각각의 id가 연관된 첨부파일(attachments)을 조회하여 메모리로 가져온다.
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "message_attachments",
      joinColumns = @JoinColumn(name = "message_id", nullable = false),
      inverseJoinColumns = @JoinColumn(name = "attachment_id", nullable = false)
  )
  @OnDelete(action = OnDeleteAction.CASCADE)
  private List<BinaryContent> attachments;

  public Message(User author, Channel channel, String content, List<BinaryContent> attachments) {
    this.author = author;
    this.channel = channel;
    this.content = content;
    this.attachments = attachments;
  }

  public void update(String content) {
    this.content = content;
  }
}