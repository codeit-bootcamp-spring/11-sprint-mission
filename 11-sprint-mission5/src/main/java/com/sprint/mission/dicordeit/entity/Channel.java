package com.sprint.mission.dicordeit.entity;

import java.io.Serializable;
import java.util.UUID;

public class Channel implements Serializable {
        private UUID id;
        private String name;
        private long createdAt;
        private long updatedAt;
        private String description;

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public String getDescription() {
        return description;
    }

    public Channel(String name, String description) {
            this.name = name;
            this.description = description;

            this.id = UUID.randomUUID();
            this.createdAt = System.currentTimeMillis();
            this.updatedAt = this.createdAt;

        }
        public void update(String newName, String newDescription){
            this.name = newName;
            this.description = newDescription;

            this.updatedAt = System.currentTimeMillis();

    }




}
