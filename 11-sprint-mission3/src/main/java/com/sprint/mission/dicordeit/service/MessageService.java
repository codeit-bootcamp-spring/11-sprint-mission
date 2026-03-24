package com.sprint.mission.dicordeit.service;

import com.sprint.mission.dicordeit.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    //메세지 기능엔 content, 보낸 userid, 받는 channelId, 보낸 시간, 수정 시간 필요

    //일단 처음 보낸진 메세지는 문장 보낸사람 받는 채널이 표시된
    public Message sendMessage(String content, UUID senderId, UUID channelId);

    //수정시간은 항상이 아닌 변수처럼 있을수도있고 없을수도있는것이니 따로 선별
    public Message correction(UUID messageId, String newContent);

    //메세지 삭제 기능 구현
    public void delete(UUID messageId);

    public Message readMessage(UUID messageId);

    public List<Message> readallMessage();

}
