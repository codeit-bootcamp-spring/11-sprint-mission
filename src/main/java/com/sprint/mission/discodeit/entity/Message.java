package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.exception.message.InvalidMessageRequestException;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Getter
public class Message extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    private String content;
    private UUID channelId;
    private UUID senderId;

    // 첨부파일 ID 리스트(BinaryContent의 id들, 첨부파일이 여러개일 수 있어서 리스트로)
    private final List<UUID> attachmentIds = new ArrayList<>();

    //생성자
    public Message(String content, UUID channelId, UUID senderId){
        super();
        this.content = content;
        this.channelId = channelId;
        this.senderId = senderId;
    }

    //getter
    public List<UUID> getAttachmentIds() {
        return Collections.unmodifiableList(this.attachmentIds);
    }

    //업데이트 메소드
    public void update(String content) { //channelid, senderid 수정할 일은 없으니 content만 수정하도록 변경함
        if(StringUtils.hasText(content)){ // 유효한 텍스트인지 검사
            this.content = content;
            updateTime();
        } else {
            throw new InvalidMessageRequestException("메시지 내용이 유효하지 않습니다.");
        }
    }

    //파일 첨부용 메서드
    //파일 첨부는 선택사항이므로 생성자로 받지 않음
    public void addAttachment(UUID attachmentId) {
        if (attachmentId != null) {
            this.attachmentIds.add(attachmentId);
            updateTime(); //
        }
    }

    @Override
    public String toString(){
        return "유저ID: "+senderId+"\n채널ID: "+channelId+"\n내용: "+content+"\n";// 임시
    }
}