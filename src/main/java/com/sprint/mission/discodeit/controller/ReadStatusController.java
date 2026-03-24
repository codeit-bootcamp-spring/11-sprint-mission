package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.readstatusdto.CreateReadStatusDto;
import com.sprint.mission.discodeit.dto.readstatusdto.ReadStatusInfoDto;
import com.sprint.mission.discodeit.service.ReadStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("/api/readStatus")
@RestController
@RequiredArgsConstructor
public class ReadStatusController {

    private final ReadStatusService readStatusService;
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<ReadStatusInfoDto> getReadStatus(@RequestBody CreateReadStatusDto readStatusDto){

        return ResponseEntity.status(200).body(readStatusService.find(readStatusDto));

    }



  @RequestMapping(method = RequestMethod.PUT)
    ResponseEntity<ReadStatusInfoDto> updateReadStatus(@RequestBody CreateReadStatusDto readStatusDto){
      readStatusService.update(readStatusDto);
      return ResponseEntity.status(200).body(readStatusService.find(readStatusDto));

    }






}
