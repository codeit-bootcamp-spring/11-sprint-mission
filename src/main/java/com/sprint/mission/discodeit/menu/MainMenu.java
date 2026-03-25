//package com.sprint.mission.discodeit.menu;
//
//import java.util.Arrays;
//import java.util.InputMismatchException;
//
//public enum MainMenu {
//    USER(1),
//    CHANNEL(2),
//    MESSAGE(3),
//    EXIT(4);
//
//    private int code;
//
//    MainMenu (int code) {
//        this.code = code;
//    }
//
//    public int getCode() {
//        return code;
//    }
//
//    public static MainMenu from(int input) {
//        return Arrays.stream(MainMenu.values())
//                .filter(m -> m.code == input)
//                .findFirst()
//                .orElseThrow(() -> new InputMismatchException("입력이 잘못되었습니다."));
//    }
//}
