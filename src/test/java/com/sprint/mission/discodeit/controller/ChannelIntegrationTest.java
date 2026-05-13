package com.sprint.mission.discodeit.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.sprint.mission.discodeit.dto.ChannelDto;
import com.sprint.mission.discodeit.dto.ChannelUpdateRequest;
import com.sprint.mission.discodeit.dto.PublicChannelCreateRequest;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
public class ChannelIntegrationTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  @DisplayName("공용 채널 생성 및 수정 API E2E 테스트")
  void createAndUpdateChannel_e2e_success() {
    PublicChannelCreateRequest createReq = new PublicChannelCreateRequest(
        "E2E채널", "설명입니다");
    ResponseEntity<ChannelDto> createRes = restTemplate.postForEntity(
        "/api/channels/public", createReq, ChannelDto.class);

    assertThat(createRes.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    ChannelDto savedChannel = createRes.getBody();
    assertThat(savedChannel).isNotNull();
    assertThat(savedChannel.name()).isEqualTo("E2E채널");

    ChannelUpdateRequest updateReq = new ChannelUpdateRequest(
        "수정된채널명", "수정된설명");
    ResponseEntity<ChannelDto> updateRes = restTemplate.exchange(
        "/api/channels/" + savedChannel.id(),
        HttpMethod.PATCH,
        new HttpEntity<>(updateReq),
        ChannelDto.class);

    assertThat(updateRes.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(updateRes.getBody()).isNotNull();
    assertThat(updateRes.getBody().name()).isEqualTo("수정된채널명");
  }

  @Test
  @DisplayName("공용 채널 삭제 API E2E 테스트")
  void deleteChannel_e2e_success() {
    PublicChannelCreateRequest createReq = new PublicChannelCreateRequest("삭제할채널", "설명");
    ResponseEntity<ChannelDto> createRes = restTemplate.postForEntity(
        "/api/channels/public", createReq, ChannelDto.class);

    UUID channelId = createRes.getBody().id();

    ResponseEntity<Void> deleteRes = restTemplate.exchange(
        "/api/channels/" + channelId,
        HttpMethod.DELETE,
        null,
        Void.class);

    assertThat(deleteRes.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }
}