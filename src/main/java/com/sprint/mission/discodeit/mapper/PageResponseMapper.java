package com.sprint.mission.discodeit.mapper;

import com.sprint.mission.discodeit.response.PageResponse;
import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;

@Component
public class PageResponseMapper {

  // cursorFunction: 페이지네이션을 해줄 요소의 커서의 성질 (시간, Id 등..)
  public <T, C> PageResponse<T> fromSlice(Slice<T> slice, Function<T, C> cursorFunction) {
    List<T> content = slice.getContent();
    C nextCursor = null;

    if (!content.isEmpty()) {
      T lastItem = content.get(content.size() - 1);
      nextCursor = cursorFunction.apply(lastItem); // 컨텐트 내 마지막 요소의 시간
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
    return new PageResponse<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.hasNext(),
        page.getTotalElements()
    );
  }
}
