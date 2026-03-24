package com.sprint.mission.dicordeit.service;

import com.sprint.mission.dicordeit.entity.Channel;

import java.util.List;
import java.util.UUID;

public interface ChannelService {

    //채팅방 만들고 그곳에 소속된 id들끼리 메세지를 주고받을수있음
    //유저 한명이 여러개의 채팅방을 가지고있을수 있음
    //방 이름을 바꿀수있음
    //방을 없앨수있음
    public Channel createChannel(String name, String description);

    public Channel readChannel(UUID channelId);

    public List<Channel> allChannel();

    public Channel updateChannel(UUID channelId, String newChannel, String newDescription);

    public void deleteChannel(UUID channelId);





}
