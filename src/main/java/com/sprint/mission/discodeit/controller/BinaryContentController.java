package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.binarycontentdto.BinaryContentInfoDto;
import com.sprint.mission.discodeit.dto.binarycontentdto.FindBinaryContetnInfo;
import com.sprint.mission.discodeit.service.BinaryContentService;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/binaryContent")
@RequiredArgsConstructor
public class BinaryContentController {

    private final BinaryContentService binaryContentService;

    @RequestMapping(value = "find", method = RequestMethod.GET)
    public ResponseEntity<BinaryContentInfoDto> findBinaryContent(@RequestParam UUID binaryContentId){

        return ResponseEntity.status(200).body(binaryContentService.find(new FindBinaryContetnInfo(binaryContentId)));

    }

    @RequestMapping(value = "/findAll",method = RequestMethod.GET)
    public ResponseEntity<List<BinaryContentInfoDto>> findAllBinaryContent(){
        return ResponseEntity.status(200).body(binaryContentService.findAll());
    }

















}
