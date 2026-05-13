package com.sprint.mission.discodeit.entity;


import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;

import java.util.UUID;
import lombok.NoArgsConstructor;

@Getter
@Table(name = "read_status")
@Entity
@NoArgsConstructor
public class ReadStatus extends BaseUpdatableEntity {

  @ManyToOne
  @JoinColumn(nullable = false, updatable = false)
  private Channel channel;
  @ManyToOne
  @JoinColumn(nullable = false, updatable = false)
  private User user;
  @Column(nullable = false)
  private Instant lastReadAt;


  public ReadStatus(User user, Channel channel, Instant lastReadAt) {
    this.channel = channel;
    this.user = user;
    this.lastReadAt = lastReadAt;
  }

  public Instant updateLastReadAt(Instant lastReadAt) {
    this.lastReadAt = lastReadAt;
    return lastReadAt;
  }


}
