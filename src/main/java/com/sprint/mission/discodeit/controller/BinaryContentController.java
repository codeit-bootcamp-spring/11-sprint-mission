package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.binarycontentdto.BinaryContentInfoDto;
import com.sprint.mission.discodeit.dto.binarycontentdto.FindBinaryContetnInfo;
import com.sprint.mission.discodeit.service.BinaryContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/binary")
@RequiredArgsConstructor
public class BinaryContentController {

    private final BinaryContentService binaryContentService;

    @RequestMapping(value = "/find", method = RequestMethod.GET)
    public ResponseEntity<BinaryContentInfoDto> findBinaryContent(@RequestBody UUID binaryContentId){

        return ResponseEntity.status(200).body(binaryContentService.find(new FindBinaryContetnInfo(binaryContentId)));

    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<BinaryContentInfoDto>> findAllBinaryContent(){
        return ResponseEntity.status(200).body(binaryContentService.findAll());
    }










}
