package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class UserStatus extends Entity{

    private final UUID userId;
    private Status status;

    public UserStatus(UUID userId) {
        this.userId = userId;
        status = isOnline();
    }

    public enum Status{
        ONLINE,
        OFFLINE
    }


    public Status isOnline(){

        Instant now = Instant.now();

        if(super.getUpdatedAt().isAfter(now.minusSeconds(300))){

            return Status.ONLINE;
        }
        else{
            return Status.OFFLINE;
        }




    }





}
