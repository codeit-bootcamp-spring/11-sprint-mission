package com.sprint.mission.discodeit.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserCreateRequest {
    private String userName;
    private String userEmail;
    private String userPassword;
    private String fileName;
    private byte[] fileContent;
    private String contentType;

}
