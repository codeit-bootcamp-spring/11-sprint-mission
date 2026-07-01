package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

@Getter
@Table(name = "message")
@Entity
@NoArgsConstructor
public class Message extends BaseUpdatableEntity {

  @Column
  private String content;


  @ManyToOne
  @JoinColumn(name = "author_id", updatable = false)
  private User author;


  @ManyToOne
  @JoinColumn(nullable = false, updatable = false)
  private Channel channel; //채널 아이디


  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @BatchSize(size = 100)
  @JoinTable(
      name = "message_attachment",
      joinColumns = @JoinColumn(name = "message_id"),
      inverseJoinColumns = @JoinColumn(name = "attachment_id")

  )
  private List<BinaryContent> attachments = new ArrayList<>();


  public Message(User author, Channel channel, String content, List<BinaryContent> attachments) {
    this.author = author;
    this.channel = channel;
    this.content = content;
    this.attachments = attachments;
  }

  public void updateContent(String content) {
    this.content = content;
  }

  public void updateAttachments(List<BinaryContent> attachments) {
    this.attachments = attachments;
  }

}
