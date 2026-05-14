package com.sprint.mission.discodeit.dto.response;

public record ErrorResponse (
  int status,
  String exceptionType,
  String message,
  Object details
) {}

