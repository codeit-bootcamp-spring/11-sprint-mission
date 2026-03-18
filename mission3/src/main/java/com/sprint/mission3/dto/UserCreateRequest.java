package com.sprint.mission3.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor


public class UserCreateRequest {
    private final String firstName;
    private final String email;
}
