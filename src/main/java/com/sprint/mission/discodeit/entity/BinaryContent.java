package com.sprint.mission.discodeit.entity;


import lombok.Getter;

import java.util.Arrays;
import java.util.UUID;

@Getter
public class BinaryContent extends Entity{


    UUID userID;
    UUID MessageId;
    String fileName;
    String contentType;
    byte[] bytes;







    public BinaryContent(UUID userID, UUID messageId, String fileName, String contentType,byte[] bytes) {
        this.fileName = fileName;
        this.userID = userID;
        MessageId = messageId;
        this.contentType = contentType;
        this.bytes = bytes;

    }

    public BinaryContent(UUID userID, String fileName,String contentType,byte[] bytes) {
        this.userID = userID;
        MessageId = null;
        this.fileName = fileName;
        this.contentType = contentType;
        this.bytes = bytes;

    }



    public void updateUpdatedAt(){

    }


    @Override
    public String toString() {
        return "BinaryContent{" +
                "userID=" + userID +
                ", MessageId=" + MessageId +
                ", fileName='" + fileName + '\'' +
                ", contentType='" + contentType + '\'' +
                ", bytes=" + Arrays.toString(bytes) +
                '}';
    }
}

