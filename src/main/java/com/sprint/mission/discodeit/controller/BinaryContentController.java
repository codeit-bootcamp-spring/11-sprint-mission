package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.BinaryContentResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/api/binary-contents")
@RequiredArgsConstructor
public class BinaryContentController {
    private final BinaryContentService binaryContentService;

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST)
    public BinaryContentResponse create(@RequestBody BinaryContentCreateRequest request){
        return binaryContentService.create(request);
    }

    @ResponseBody
    @RequestMapping(value = "/find", method = RequestMethod.GET)
    public ResponseEntity<BinaryContentResponse> find(
            @RequestParam UUID binaryContentId
    ){
        return ResponseEntity.ok(binaryContentService.findEntitybyId(binaryContentId));
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET)
    public List<BinaryContentResponse> findAllByIdIn(@RequestParam List<UUID> ids){
        return binaryContentService.findAllByIdIn(ids);
    }

    @ResponseBody
    @RequestMapping(value = "/{binaryContentId}")
    public void delete(@PathVariable UUID binaryContentId){
        binaryContentService.delete(binaryContentId);
    }
}
