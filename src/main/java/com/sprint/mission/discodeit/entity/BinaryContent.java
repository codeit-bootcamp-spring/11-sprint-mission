package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.binary.BinaryFile;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class BinaryContent extends Entity{

    UUID userID;
    UUID MessageId;
    Type type;
    BinaryFile binaryFile;
    //String url;



    public enum Type{
        PROFILEIMG,
        IMAGE,
        VIDEO,
        AUDIO,
        FILE
    }

    public BinaryContent(UUID userID, UUID messageId, Type type, BinaryFile binaryFile) {
        this.userID = userID;
        this.type = type;
        MessageId = messageId;
        this.binaryFile = binaryFile;

    }

    public BinaryContent(UUID userID,BinaryFile binaryFile) {
        this.userID = userID;
        this.type = Type.PROFILEIMG;
        MessageId = null;
        this.binaryFile = binaryFile;
    }



    public void updateUpdatedAt(){



    }

    @Override
    public String toString() {
        return "BinaryContent{" +
                "userID=" + userID +
                ", MessageId=" + MessageId +
                ", type=" + type +

                '}';
    }
}
