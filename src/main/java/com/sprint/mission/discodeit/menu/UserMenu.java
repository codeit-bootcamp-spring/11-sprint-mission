//package com.sprint.mission.discodeit.menu;
//
//import java.util.Arrays;
//import java.util.InputMismatchException;
//
//public enum UserMenu {
//    CREATE_USER(1),
//    READ_USER(2),
//    READ_ALL(3),
//    DELETE(4),
//    UPDATE(5),
//    BACK(0);
//
//    private int userNumber;
//
//    UserMenu(int number) {
//        this.userNumber = number;
//    }
//
//    public static UserMenu from(int input) {
//        return Arrays.stream(UserMenu.values())
//                .filter(m -> m.userNumber == input)
//                .findFirst()
//                .orElseThrow(() -> new InputMismatchException("입력이 잘못되었습니다."));
//    }
//}
