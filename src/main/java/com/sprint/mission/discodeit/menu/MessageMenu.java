//package com.sprint.mission.discodeit.menu;
//
//import java.util.Arrays;
//import java.util.InputMismatchException;
//
//public enum MessageMenu {
//    CREATE_MESSAGE(1),
//    READ_MESSAGE(2),
//    READ_ALL(3),
//    DELETE(4),
//    UPDATE(5),
//    BACK(0);
//
//    private int messageNumber;
//
//    MessageMenu(int number) {
//        this.messageNumber = number;
//    }
//
//    public static MessageMenu from(int input) {
//        return Arrays.stream(MessageMenu.values())
//                .filter(m -> m.messageNumber == input)
//                .findFirst()
//                .orElseThrow(() -> new InputMismatchException("입력이 잘못되었습니다."));
//    }
//}
