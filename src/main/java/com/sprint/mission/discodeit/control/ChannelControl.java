package com.sprint.mission.discodeit.control;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.menu.ChannelMenu;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class ChannelControl {
    public static void controlChannelMenu(Scanner input, ChannelService channelService) {
        while(true) {
            System.out.println(
                    "== Create(1) == Read(2) == ReadAll(3) == Delete(4) == Update(5) == Back(0)"
            );

            try {
                int num = Integer.parseInt(input.nextLine());
                ChannelMenu menu = ChannelMenu.from(num);

                switch(menu) {
                    case CREATE_CHANNEL:
                        try {
                            System.out.print("등록할 채널명을 입력해주세요 : ");
                            String name = input.nextLine();
                            System.out.println();
                            System.out.print("채널 설명을 입력해주세요 : ");
                            String description = input.nextLine();
                            System.out.println();
                            System.out.println("채널 타입을 입력해주세요 (1. PUBLIC, 2. PRIVATE) : ");
                            int typeNum = Integer.parseInt(input.nextLine());
                            ChannelType type = (typeNum == 2) ? ChannelType.PRIVATE : ChannelType.PUBLIC;
                            channelService.createChannel(name, description, type);
                        } catch (IllegalArgumentException e) {
                            System.out.println("잘못된 입력입니다.");
                        } catch (RuntimeException e) {
                            System.out.println(e.getMessage());
                        }
                        break;
                    case READ_CHANNEL:
                        while(true) {
                            try {
                                List<Channel> channels = channelService.allReadChannel();
                                if(channels.isEmpty()) {
                                    System.out.println("채널 목록이 비어있습니다.");
                                    break;
                                }
                                for (int i = 0; i < channels.size(); i++) {
                                    Channel c = channels.get(i);
                                    System.out.println((i + 1) + ". 채널명 : " + c.getName());
                                }
                                System.out.print("조회 할 채널을 선택해주세요 : ");
                                int channelChoice = Integer.parseInt(input.nextLine());
                                if(channelChoice < 1 || channelChoice > channels.size()) {
                                    System.out.println("목록에 없는 번호입니다. 다시 입력해주세요.");
                                    continue;
                                }

                                UUID id = channels.get(channelChoice - 1).getId();
                                Channel resultChannel = channelService.readChannel(id);
                                System.out.println("조회 결과 : " + resultChannel);
                                break;
                            } catch (IllegalArgumentException e) {
                                System.out.println("해당 채널이 존재하지 않습니다.");
                            } catch (RuntimeException e) {
                                System.out.println(e.getMessage());
                            }
                        }
                        break;
                    case READ_ALL:
                        try {
                            List<Channel> readAllChannelList = channelService.allReadChannel();
                            if(readAllChannelList.isEmpty()) {
                                System.out.println("채널 목록이 비어있습니다.");
                                break;
                            }
                            for (int i = 0; i < readAllChannelList.size(); i++) {
                                Channel rc = readAllChannelList.get(i);
                                System.out.println((i + 1) + ". 채널명 : " + rc.getName() + ", 생성시간 : " + rc.getCreatedAt());
                            }
                        } catch (RuntimeException e) {
                            System.out.println(e.getMessage());
                        }
                        break;
                    case DELETE:
                        while(true) {
                            try {
                                List<Channel> deleteChannelList = channelService.allReadChannel();
                                if(deleteChannelList.isEmpty()) {
                                    System.out.println("채널 목록이 비어있습니다.");
                                    break;
                                }
                                for (int i = 0; i < deleteChannelList.size(); i++) {
                                    Channel dc = deleteChannelList.get(i);
                                    System.out.println((i + 1) + ". 채널명 : " + dc.getName() + ", 생성시간 : " + dc.getCreatedAt());
                                }
                                System.out.print("삭제 할 채널을 선택해주세요 : ");
                                int choice = Integer.parseInt(input.nextLine());
                                System.out.println();
                                if(choice < 1 || choice > deleteChannelList.size()) {
                                    System.out.println("목록에 없는 번호입니다. 다시 입력해주세요.");
                                    continue;
                                }

                                UUID id = deleteChannelList.get(choice - 1).getId();
                                channelService.deleteChannel(id);
                                System.out.println("삭제되었습니다.");
                                break;
                            } catch (IllegalArgumentException e) {
                                System.out.println("채널이 존재하지 않습니다.");
                            } catch (RuntimeException e) {
                                System.out.println(e.getMessage());
                            }
                        }
                        break;
                    case UPDATE:
                        while(true) {
                            try {
                                List<Channel> updateChannelList = channelService.allReadChannel();
                                if(updateChannelList.isEmpty()) {
                                    System.out.println("채널 목록이 비어있습니다.");
                                    break;
                                }
                                for (int i = 0; i < updateChannelList.size(); i++) {
                                    Channel uc = updateChannelList.get(i);
                                    System.out.println((i + 1) + ". 채널명 : " + uc.getName() + ", 생성시간 : " + uc.getCreatedAt());
                                }
                                System.out.print("변경 할 채널을 선택해주세요 : ");
                                int updateChoice = Integer.parseInt(input.nextLine());
                                if(updateChoice < 1 || updateChoice > updateChannelList.size()) {
                                    System.out.println("목록에 없는 번호입니다. 다시 입력해주세요");
                                    continue;
                                }

                                UUID id = updateChannelList.get(updateChoice - 1).getId();
                                System.out.print("변경 할 채널이름을 입력해주세요 : ");
                                String updateName = input.nextLine();
                                System.out.println();
                                System.out.print("변경 할 설명을 입력해주세요 : ");
                                String description = input.nextLine();
                                System.out.println();
                                channelService.updateChannel(id, updateName, description);
                                break;
                            } catch (IllegalArgumentException e) {
                                System.out.println("채널이 존재하지 않습니다.");
                            } catch (RuntimeException e) {
                                System.out.println(e.getMessage());
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
}
