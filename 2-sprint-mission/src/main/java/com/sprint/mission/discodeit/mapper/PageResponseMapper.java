package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@Component
public class PageResponseMapper {

  // Slice
  public <T> PageResponse<T> fromSlice(Slice<T> slice, Object nextCursor) {
    return PageResponse.<T>builder()
        .content(slice.getContent())
        .nextCursor(nextCursor)
        .size(slice.getSize())
        .hasNext(slice.hasNext())
        .build();
  }

  // Page
  public <T> PageResponse<T> fromPage(Page<T> page, Object nextCursor) {
    return PageResponse.<T>builder()
        .content(page.getContent())
        .nextCursor(nextCursor)
        .size(page.getSize())
        .hasNext(page.hasNext())
        .totalElements(page.getTotalElements())
        .build();
  }
}