package com.sprint.mission.discodeit.dto.messagedto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record MessageCreateRequest(
    @NotNull(message = "메시지 내용은 null이 될 수 없습니다.")
    String content,
    @NotNull(message = "채널 아이디는 필수 입력값입니다.")
    UUID channelId,
    @NotNull(message = "작성자 아이디는 필수 입력값입니다.")
    UUID authorId

) {

}
