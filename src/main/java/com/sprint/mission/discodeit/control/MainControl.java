package com.sprint.mission.discodeit.control;

import com.sprint.mission.discodeit.menu.MainMenu;

import java.util.InputMismatchException;
import java.util.Scanner;

public class MainControl {
    public static MainMenu selectMainMenu(Scanner input) {
        System.out.println("메뉴 번호를 선택하세요. User(1) Channel(2), Message(3), EXIT(4)");

        try {
            int num = Integer.parseInt(input.nextLine());
            return MainMenu.from(num);

        } catch (InputMismatchException e) {
            System.out.println("입력이 잘못되었습니다. 다시 입력하세요.");
            input.nextLine();
            return selectMainMenu(input);
        }
    }
}
