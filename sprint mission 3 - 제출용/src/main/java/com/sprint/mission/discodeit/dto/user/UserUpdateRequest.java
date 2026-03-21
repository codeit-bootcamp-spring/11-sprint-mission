package com.sprint.mission.discodeit.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserUpdateRequest {
    private UUID id;
    private String userName;
    private String userEmail;
    private String userPassword;
    private String fileName;
    private byte[] fileContent;
    private String contentType;
}
