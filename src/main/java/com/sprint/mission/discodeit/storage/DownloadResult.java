package com.sprint.mission.discodeit.storage;

import org.springframework.core.io.Resource;

public sealed interface DownloadResult {

  record Stream(Resource resource, String fileName, String contentType, long size)
      implements DownloadResult {

  }

  record Redirect(String url) implements DownloadResult {

  }
}