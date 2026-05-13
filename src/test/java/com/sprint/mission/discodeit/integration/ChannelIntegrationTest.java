package com.sprint.mission.discodeit.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sprint.mission.discodeit.dto.channeldto.ChannelDto;
import com.sprint.mission.discodeit.dto.channeldto.request.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.channeldto.request.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.MethodArgumentNotValidException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Transactional
public class ChannelIntegrationTest {

  @Autowired
  private TestRestTemplate restTemplate;


  @Test
  @DisplayName("공개채널 생성")
  void createPublicChannel() {

    PublicChannelCreateRequest request = new PublicChannelCreateRequest("채널 이름", "채널 설명");

    ResponseEntity<ChannelDto> response = restTemplate.postForEntity(
        "/api/channels/public", request, ChannelDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody().name()).isEqualTo("채널 이름");
    assertThat(response.getBody().type()).isEqualTo(Channel.ChannelType.PUBLIC);

  }

  @Test
  @DisplayName("개인 채널 예외처리 ")
  void createPrivateChannelFailByNullMember() {

    PrivateChannelCreateRequest request =
        new PrivateChannelCreateRequest(null);

    ResponseEntity<ChannelDto> response = restTemplate.postForEntity(
        "/api/channels/private", request, ChannelDto.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);


  }


}
