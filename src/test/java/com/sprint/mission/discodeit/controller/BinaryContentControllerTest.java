package com.sprint.mission.discodeit.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.mission.discodeit.dto.data.BinaryContentDto;
import com.sprint.mission.discodeit.service.BinaryContentService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BinaryContentController.class)
class BinaryContentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    BinaryContentService binaryContentService;

    @MockBean
    BinaryContentStorage binaryContentStorage;

    private BinaryContentDto binaryContentDto(UUID id) {
        return new BinaryContentDto(id, "file.png", 1024L, "image/png");
    }

    @Test
    void find_정상_200반환() throws Exception {
        UUID id = UUID.randomUUID();
        given(binaryContentService.find(id)).willReturn(binaryContentDto(id));

        mockMvc.perform(get("/api/binaryContents/{binaryContentId}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fileName").value("file.png"))
            .andExpect(jsonPath("$.contentType").value("image/png"));
    }

    @Test
    void findAllByIdIn_정상_200반환() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        given(binaryContentService.findAllByIdIn(List.of(id1, id2)))
            .willReturn(List.of(binaryContentDto(id1), binaryContentDto(id2)));

        mockMvc.perform(get("/api/binaryContents")
                .param("binaryContentIds", id1.toString(), id2.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void download_정상_200반환() throws Exception {
        UUID id = UUID.randomUUID();
        BinaryContentDto dto = binaryContentDto(id);
        given(binaryContentService.find(id)).willReturn(dto);
        given(binaryContentStorage.download(dto)).willReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/api/binaryContents/{binaryContentId}/download", id))
            .andExpect(status().isOk());
    }
}