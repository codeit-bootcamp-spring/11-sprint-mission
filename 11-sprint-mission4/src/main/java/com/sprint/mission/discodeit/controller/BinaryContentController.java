package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class BinaryContentController {

    private final BinaryContentService binaryContentService;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public BinaryContent find(@PathVariable UUID id) {
        return binaryContentService.find(id);
    }

    @RequestMapping(method = RequestMethod.POST)
    public List<BinaryContent> findAll(@RequestBody List<UUID> ids) {
        return binaryContentService.findAllByIdIn(ids);
    }

}
