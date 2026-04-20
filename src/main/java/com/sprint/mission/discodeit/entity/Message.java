package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

@Getter
@Entity
@Table(name = "messages")
@NoArgsConstructor
public class Message extends BaseUpdatableEntity {

  // 필드
  // content text
  @Column(name = "content", columnDefinition = "text")
  private String content; // 메시지 내용

  // 연관 관계 필드
  // 채널이 삭제될 때 메시지도 삭제되어야 한다.
  // channel_id uuid not null references channels (id) on delete cascade
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "channel_id", nullable = false)
  private Channel channel;

  //author_id uuid references users (id) on delete set null
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "author_id")
  private User author;

  // N:M 관계(messages:binary_contents)
  // 중계 테이블 message_attachments / 중계 엔티티 Message
  // 의 message_id를 현재 엔티티의 외래키
  // 의 attachment_id는 반대쪽 엔티티(BinaryContent)의 외래키
  // (관계가 양방향일 경우 반대쪽 엔티티에도 @ManyToMany 필요)
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "message_attachments",
      joinColumns = @JoinColumn(name = "message_id"),
      inverseJoinColumns = @JoinColumn(name = "attachment_id"))
  @BatchSize(size = 50)
  private List<BinaryContent> attachments;

  private Message(String content, Channel channel, User author) {
    this.content = content;
    this.channel = channel;
    this.author = author;
    this.attachments = new ArrayList<>(); // 삽입/삭제보다 조회가 더 많이 일어나기 때문에 LinkedList가 아닌 ArrayList 사용
  }


  // 정적 팩토리 메서드
  public static Message create(String content, Channel channel, User author) {
    return new Message(content, channel, author);
  }

  // add Attachment(캡슐화)
  public void addAttachment(BinaryContent attachment) {
    this.attachments.add(attachment);
  }

  // getter(Lombok의 @Getter로 대체)

  // update
  public void updateContent(String content) {
    this.content = content;
  }

  @Override
  public String toString() {
    return " 메시지 내용 : " + content
        + "\n 메시지가 작성된 채널 : " + channel.getName()
        + ", 메시지 작성자 ID : " + author.getUsername();
  }
}