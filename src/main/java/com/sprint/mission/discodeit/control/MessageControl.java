package com.sprint.mission.discodeit.control;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.menu.MessageMenu;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class MessageControl {
    public static void controlMessageMenu(Scanner input, MessageService messageService, UserService userService, ChannelService channelService, UUID currentUserId) {

        UUID currentChannelId = selectChannel(input, channelService, currentUserId);
        if(currentChannelId == null) return;

        while(true) {
            String channelName = channelService.readChannel(currentChannelId).getName();
            System.out.println("\n---  [" + channelName + " 채널] 메시지 관리 ---");
            System.out.println("1.메시지 전송 | 2.상세 조회 | 3.전체 보기 | 4.삭제 | 5.수정 | 0.뒤로가기");
            System.out.print("선택: ");

            try {
                int num = Integer.parseInt(input.nextLine());
                MessageMenu menu = MessageMenu.from(num);

                switch(menu) {
                    case CREATE_MESSAGE:
                        try {
                            System.out.print("메세지를 입력해주세요 : ");
                            String createMessage = input.nextLine();
                            messageService.createMessage(currentChannelId, currentUserId, createMessage);
                        } catch (RuntimeException e) {
                            System.out.println(e.getMessage());
                        }
                        break;
                    case READ_MESSAGE:
                        while(true) {
                            try {
                                List<Message> messageList = messageService.readMessagesByChannel(currentChannelId);
                                if(messageList.isEmpty()) {
                                    System.out.println("이 채널에 메시지가 없습니다.");
                                    break;
                                }
                                for (int i = 0; i < messageList.size(); i++) {
                                    Message m = messageList.get(i);
                                    User author = userService.readUser(m.getAuthorId());
                                    String authorName;

                                    if(author == null) {
                                        authorName = "탈퇴한 유저(또는 테스트용)";
                                    } else {
                                        authorName = author.getUsername();
                                    }
                                    System.out.println((i + 1) + ". [" + authorName + "] " + m.getMessage());
                                }
                                System.out.print("조회 할 메세지를 선택해주세요 : ");
                                int readMessageChoice = Integer.parseInt(input.nextLine());

                                if(readMessageChoice < 1 || readMessageChoice > messageList.size()) {
                                    System.out.println("목록에 없는 번호입니다. 다시 입력해주세요.");
                                    continue;
                                }

                                UUID id = messageList.get(readMessageChoice - 1).getId();
                                Message resultMessage = messageService.readMessage(id);
                                User author = userService.readUser(resultMessage.getAuthorId());
                                System.out.println("\n[메시지 상세 정보]");
                                System.out.println("작성자 : " + author.getUsername());
                                System.out.println("내용 : " + resultMessage.getMessage());
                                System.out.println("생성 시간 : " + resultMessage.getCreatedAt());
                                System.out.println("수정 시간 : " + resultMessage.getUpdatedAt());
                                break;
                            } catch (IllegalArgumentException e) {
                                System.out.println("메세지가 존재하지 않습니다.");
                            } catch (RuntimeException e) {
                                System.out.println(e.getMessage());
                            }
                        }
                        break;
                    case READ_ALL:
                        try {
                            List<Message> allReadMessageList = messageService.readMessagesByChannel(currentChannelId);
                            if(allReadMessageList.isEmpty()) {
                                System.out.println("메세지 목록이 비어있습니다.");
                                break;
                            }
                            for (int i = 0; i < allReadMessageList.size(); i++) {
                                Message m = allReadMessageList.get(i);
                                User author = userService.readUser(m.getAuthorId());
                                String authorName;

                                if(author == null) {
                                    authorName = "탈퇴한 유저(또는 테스트용)";
                                } else {
                                    authorName = author.getUsername();
                                }
                                System.out.println((i + 1) + ". [" + authorName + "] " + m.getMessage());
                            }
                        } catch (RuntimeException e) {
                            System.out.println(e.getMessage());
                        }
                        break;
                    case DELETE:
                        while(true) {
                            try {
                                List<Message> delMessageList = messageService.readMessagesByChannel(currentChannelId);
                                if(delMessageList.isEmpty()) {
                                    System.out.println("삭제할 메세지가 없습니다.");
                                    break;
                                }
                                for (int i = 0; i < delMessageList.size(); i++) {
                                    Message m = delMessageList.get(i);
                                    User author = userService.readUser(m.getAuthorId());
                                    String authorName;

                                    if(author == null) {
                                        authorName = "탈퇴한 유저(또는 테스트용)";
                                    } else {
                                        authorName = author.getUsername();
                                    }
                                    System.out.println((i + 1) + ". [" + authorName + "] " + m.getMessage());
                                }
                                System.out.print("삭제 할 메세지를 선택 : ");
                                int choice = Integer.parseInt(input.nextLine());
                                if(choice < 1 || choice > delMessageList.size()) {
                                    System.out.println("목록에 없는 번호입니다. 다시 입력해주세요.");
                                    continue;
                                }

                                UUID delId = delMessageList.get(choice - 1).getId();
                                messageService.deleteMessage(delId);
                                System.out.println("삭제되었습니다.");
                                break;
                            } catch (IllegalArgumentException e) {
                                System.out.println("메세지가 존재하지 않습니다.");
                            } catch (RuntimeException e) {
                                System.out.println(e.getMessage());
                            }
                        }
                        break;
                    case UPDATE:
                        while(true) {
                            try {
                                List<Message> updateMessageList = messageService.readMessagesByChannel(currentChannelId);
                                if(updateMessageList.isEmpty()) {
                                    System.out.println("메세지 목록이 비어있습니다.");
                                    break;
                                }
                                for (int i = 0; i < updateMessageList.size(); i++) {
                                    Message m = updateMessageList.get(i);
                                    User author = userService.readUser(m.getAuthorId());
                                    String authorName;

                                    if(author == null) {
                                        authorName = "탈퇴한 유저(또는 테스트용)";
                                    } else {
                                        authorName = author.getUsername();
                                    }
                                    System.out.println((i + 1) + ". [" + authorName + "] " + m.getMessage());
                                }
                                System.out.print("변경 할 메세지를 선택해주세요 : ");
                                int updateChoice = Integer.parseInt(input.nextLine());
                                if(updateChoice < 1 || updateChoice > updateMessageList.size()) {
                                    System.out.println("목록에 없는 번호입니다. 다시 입력해주세요.");
                                    continue;
                                }

                                UUID updateId = updateMessageList.get(updateChoice - 1).getId();
                                System.out.print("변경 할 메세지를 입력해주세요 : ");
                                String newMessage = input.nextLine();

                                messageService.updateMessage(updateId, newMessage);
                                System.out.println("수정 완료!");
                                break;
                            } catch (IllegalArgumentException e) {
                                System.out.println("업데이트 할 메세지가 존재하지 않습니다.");
                            } catch (RuntimeException e) {
                                System.out.println(e.getMessage());
                                break;
                            }
                        }
                        break;
                    case BACK:
                        System.out.println("메인메뉴로 돌아갑니다.");
                        return;
                }
            } catch (InputMismatchException e) {
                System.out.println("입력이 잘못되었습니다. 다시 입력하세요.");
                input.nextLine();
            }
        }
    }
    private static UUID selectChannel(Scanner input, ChannelService channelService, UUID currentUserId) {
        while(true) {
            List<Channel> channels = channelService.allReadChannel();
            if(channels.isEmpty()) {
                System.out.println("생성된 채널이 없습니다.");
                return null;
            }
            System.out.println("\n--- 접속 가능한 채널 목록 ---");
            for (int i = 0; i < channels.size(); i++) {
                Channel c = channels.get(i);

                System.out.println((i + 1) + ". [" + c.getType() + "] " + c.getName());
            }
            System.out.println("0. 돌아가기");
            System.out.print("입장할 채널 번호 선택 : ");

            try {
                int choice = Integer.parseInt(input.nextLine());
                if(choice == 0) return null;
                if(choice < 1 || choice > channels.size()) {
                    System.out.println("번호를 잘못 입력하셨습니다.");
                    continue;
                }
                Channel selected = channels.get(choice - 1);

                if(selected.getType() == ChannelType.PRIVATE && !selected.getOwnerId().equals(currentUserId)) {
                    System.out.println("접근 권한이 없습니다.");
                    continue;
                }
                return selected.getId();
            } catch (NumberFormatException e) {
                System.out.println("숫자만 입력 가능합니다.");
            }
        }
    }
}
