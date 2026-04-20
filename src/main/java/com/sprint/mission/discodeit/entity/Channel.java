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

  // type varchar(10) not null check (type in ('public', 'private'))
  @Enumerated(EnumType.STRING) // Enum
  @Column(name = "type", length = 10, nullable = false)
  private ChannelType type;

  // name varchar(100)
  @Column(name = "name", length = 100)
  private String name;

  // description varchar(500)
  @Column(name = "description", length = 500)
  private String description;


  // 코드 탬플릿에 적합한 생성자 오버로딩
  private Channel(ChannelType type, String name, String description) {
    this.type = type;
    this.name = name;
    this.description = description;
  }

  //    // 정적 팩토리 메서드(Private 채널 생성)
//    public static Channel create(Type channelType, String name, String description) {
//        return new Channel(channelType, name, description);
//    }
  // 정적 팩토리 메서드(Private 채널 생성)
  public static Channel createPrivate() {
    return new Channel(ChannelType.PRIVATE, null, null);
  }

  // 정적 팩토리 메서드(Public 채널 생성)
  public static Channel createPublic(String name, String description) {
    return new Channel(ChannelType.PUBLIC, name, description);
  }

  // getter(Lombok의 @Getter로 대체)

  // update
  public void updateName(String name) {
    this.name = name;
  }

  public void updateDescription(String description) {
    this.description = description;
  }

  @Override
  public String toString() {
    return "채널 이름 : " + name + ", 채널 설명 : " + description;
  }

  public enum ChannelType {
    PUBLIC, PRIVATE;
  }
}
