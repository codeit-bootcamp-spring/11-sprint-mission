//package com.sprint.mission.discodeit.menu;
//
//import java.util.Arrays;
//import java.util.InputMismatchException;
//
//public enum ChannelMenu {
//    CREATE_CHANNEL(1),
//    READ_CHANNEL(2),
//    READ_ALL(3),
//    DELETE(4),
//    UPDATE(5),
//    BACK(0);
//
//    private int channelNumber;
//
//    ChannelMenu(int number) {
//        this.channelNumber = number;
//    }
//
//    public static ChannelMenu from(int input) {
//        return Arrays.stream(ChannelMenu.values())
//                .filter(m -> m.channelNumber == input)
//                .findFirst()
//                .orElseThrow(() -> new InputMismatchException("입력이 잘못되었습니다."));
//    }
//}
