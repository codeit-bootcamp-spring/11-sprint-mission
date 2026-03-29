package com.sprint.mission.discodeit.dto.channel;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PublicChannelCreateRequest {
    @NotBlank
    private String channelName;
    private UUID adminId;
}