package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.response.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@Component
public class PageResponseMapper {

  public <T> PageResponse<T> fromSlice(Slice<T> slice) {
    return fromSlice(slice, null);
  }

  public <T> PageResponse<T> fromSlice(Slice<T> slice, Object nextCursor) {
    if (slice == null) {
      return null;
    }

    return new PageResponse<>(
        slice.getContent(),
        nextCursor,
        slice.getSize(),
        slice.hasNext(),
        null
    );
  }

  public <T> PageResponse<T> fromPage(Page<T> page) {
    if (page == null) {
      return null;
    }

    return new PageResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.hasNext(),
        page.getTotalElements()
    );
  }
}
