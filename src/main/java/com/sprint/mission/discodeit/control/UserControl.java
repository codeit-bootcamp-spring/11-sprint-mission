package com.sprint.mission.discodeit.control;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.menu.UserMenu;
import com.sprint.mission.discodeit.service.UserService;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class UserControl {
    public static UUID controlUserMenu(Scanner input, UserService userService) {
        while(true) {
            System.out.println("\n== 유저 관리 시스템 ==");
            System.out.println("1.생성 | 2.선택(조회) | 3.목록 | 4.삭제 | 5.수정 | 0.뒤로가기");
            System.out.print("입력: ");

            try {
                String line = input.nextLine();
                int num = Integer.parseInt(line);
                UserMenu menu = UserMenu.from(num);

                switch (menu) {
                    case CREATE_USER:
                        try {
                            System.out.print("등록할 이름을 입력해주세요 : ");
                            String name = input.nextLine();
                            System.out.println();
                            System.out.print("이메일을 입력해주세요 : ");
                            String email = input.nextLine();
                            System.out.println();
                            System.out.print("패스워드를 입력해주세요 : ");
                            String password = input.nextLine();
                            System.out.println();

                            User newUser = userService.createUser(name, email, password);
                            System.out.println("유저 생성 및 로그인 성공!");
                            return newUser.getId();
                        } catch (IllegalArgumentException e) {
                            System.out.println("이미 존재하는 이메일입니다.");
                        } catch (RuntimeException e) {
                            System.out.println(e.getMessage());
                        }
                        break;
                    case READ_USER:
                        while(true) {
                            try {
                                List<User> readUserList = userService.allReadUser();
                                if(readUserList.isEmpty()) {
                                    System.out.println("유저 목록이 비어있습니다.");
                                    break;
                                }
                                for (int i = 0; i < readUserList.size(); i++) {
                                    User ru = readUserList.get(i);
                                    System.out.println((i + 1) + ". 유저이름 : " + ru.getUsername() + ", (" + ru.getEmail() + ")");
                                }
                                System.out.print("조회 할 유저번호를 선택해주세요 : ");
                                int choiceReadUser = Integer.parseInt(input.nextLine());
                                System.out.println();
                                if(choiceReadUser < 1 || choiceReadUser > readUserList.size()) {
                                    System.out.println("목록에 없는 번호입니다. 다시 입력해주세요.");
                                    continue;
                                }
                                User readUser = readUserList.get(choiceReadUser - 1);
                                System.out.println(readUser.getUsername() + "님으로 로그인 되었습니다.");
                                return readUser.getId();
                            } catch (IllegalArgumentException e) {
                                System.out.println("이메일이 잘못 입력되었습니다.");
                            } catch (RuntimeException e) {
                                System.out.println(e.getMessage());
                            }
                        }
                        break;
                    case READ_ALL:
                        try {
                            List<User> userList = userService.allReadUser();
                            if(userList.isEmpty()) {
                                System.out.println("유저 목록이 비어있습니다.");
                                break;
                            } else {
                                userList.forEach(System.out::println);
                            }
                        } catch (RuntimeException e) {
                            System.out.println(e.getMessage());
                        }
                        break;
                    case DELETE:
                        while(true) {
                            try {
                                List<User> deleteUserList = userService.allReadUser();
                                if(deleteUserList.isEmpty()) {
                                    System.out.println("유저 목록이 비어있습니다.");
                                    break;
                                }
                                for (int i = 0; i < deleteUserList.size(); i++) {
                                    User du = deleteUserList.get(i);
                                    System.out.println((i + 1) + ". 유저이름 : " + du.getUsername() + ", 생성시간 : " + du.getCreatedAt());
                                }
                                System.out.print("삭제 할 유저를 선택해주세요 : ");
                                int choiceDeleteUser = Integer.parseInt(input.nextLine());
                                System.out.println();
                                if(choiceDeleteUser < 1 || choiceDeleteUser > deleteUserList.size()) {
                                    System.out.println("목록에 없는 번호입니다. 다시 입력해주세요.");
                                    continue;
                                }
                                User deleteUser = deleteUserList.get(choiceDeleteUser - 1);

                                System.out.print("패스워드를 입력해주세요 : ");
                                String deletePassword = input.nextLine();
                                System.out.println();

                                userService.deleteUser(deleteUser.getId(), deletePassword);
                                System.out.println("삭제되었습니다.");
                                break;
                            } catch (InputMismatchException e) {
                                System.out.println("패스워드가 일치하지 않습니다.");
                            } catch (IllegalArgumentException e) {
                                System.out.println("유저가 없습니다.");
                            } catch (RuntimeException e) {
                                System.out.println(e.getMessage());
                            }
                        }
                        break;
                    case UPDATE:
                        while(true) {
                            try {
                                List<User> users = userService.allReadUser();
                                if(users.isEmpty()) {
                                    System.out.println("유저 목록이 비어있습니다.");
                                    break;
                                }
                                for (int i = 0; i < users.size(); i++) {
                                    User u = users.get(i);
                                    System.out.println((i + 1) + ". " + u.getUsername() + ", " + u.getEmail());
                                }
                                System.out.print("변경 할 유저를 선택해주세요 : ");
                                int choice = Integer.parseInt(input.nextLine());
                                if(choice < 1 || choice > users.size()) {
                                    System.out.println("목록에 없는 번호입니다. 다시 입력해주세요.");
                                    continue;
                                }

                                User choiceUser = users.get(choice - 1);

                                System.out.print("변경 할 이름을 입력해주세요 : ");
                                String updateName = input.nextLine();
                                System.out.println();
                                System.out.print("변경 할 이메일을 입력해주세요 : ");
                                String updateEmail = input.nextLine();
                                System.out.println();
                                System.out.print("변경 할 패스워드를 입력해주세요 : ");
                                String updatePassword = input.nextLine();
                                System.out.println();
                                userService.updateUser(choiceUser.getId(), updateName, updateEmail, updatePassword);
                                System.out.println("정보가 수정되었습니다.");
                                break;
                            } catch (IllegalArgumentException e) {
                                System.out.println("존재하지 않는 유저입니다.");
                            } catch (RuntimeException e) {
                                System.out.println(e.getMessage());
                            }
                        }
                        break;
                    case BACK:
                        System.out.println("메인메뉴로 돌아갑니다.");
                        return null;
                }
            } catch (InputMismatchException e) {
                System.out.println("입력이 잘못되었습니다. 다시 입력하세요.");
                input.nextLine();
            }
        }
    }
}
