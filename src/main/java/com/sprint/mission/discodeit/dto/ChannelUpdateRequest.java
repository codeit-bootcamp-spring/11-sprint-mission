package com.sprint.mission.discodeit.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PublicChannelUpdateRequest")
public record ChannelUpdateRequest(String newName, String newDescription) {

}
