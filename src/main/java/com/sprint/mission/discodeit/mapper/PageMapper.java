package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.dto.common.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@Component
public class PageMapper {

  public <T> PageResponse<T> fromSlice(Slice<T> slice) {
    return new PageResponse<T>(
        slice.getContent(),
        slice.getNumber(),
        slice.getSize(),
        slice.hasNext(),
        null
    );
  }

  public <T> PageResponse<T> fromPage(Page<T> page) {
    return new PageResponse<T>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.hasNext(),
        page.getTotalElements()
    );
  }
}
