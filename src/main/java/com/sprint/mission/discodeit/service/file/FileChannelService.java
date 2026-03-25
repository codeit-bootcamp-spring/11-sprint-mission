//package com.sprint.mission.discodeit.service.file;
//
//import com.sprint.mission.discodeit.entity.Channel;
//import com.sprint.mission.discodeit.repository.ChannelRepository;
//import com.sprint.mission.discodeit.service.ChannelService;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//public class FileChannelService implements ChannelService {
//    // 기존 JCF Service의 경우 프로그램 종료 시 메모리에서만 존재하기 때문에 프로그램 종료 시 동시에 데이터가 사라진다.
//    // FileSystem을 통해 데이터를 남겨놓기
//    // 직렬화 : Java 객체 -> 바이트 배열 -> 파일
//    // 역직렬화 : 파일 -> 바이트 배열 -> Java 객체
//
//    // 직렬화(Save -> Create를 저장, Update를 저장, Delete를 저장), 역직렬화(Load -> 불러오기)
//
////    private final Map<UUID, Channel> channels = new HashMap<>();
////
////    // 저장 메서드 save(직렬화)
////    public void save() {
////        try (FileOutputStream fos = new FileOutputStream("channels.ser");
////             ObjectOutputStream oos = new ObjectOutputStream(fos);
////        ) {
////            oos.writeObject(channels);
////        }
////        catch (IOException e) {
////            e.printStackTrace();
////        }
////    }
////
////    // 불러오기 메서드 load(역직렬화)
////    public void load() {
////        try (FileInputStream fis = new FileInputStream("channels.ser");
////             ObjectInputStream ois = new ObjectInputStream(fis)) {
////            Map<UUID, Channel> loadChannels = (Map<UUID, Channel>) ois.readObject();
////            channels.clear(); // 한 번 비우고
////            channels.putAll(loadChannels); // 불러온다.(기존에 있던 데이터까지 같이 로드될 수 있기 때문에)
////        } catch (IOException | ClassNotFoundException e) {
////            e.printStackTrace();
////        }
////
////    }
////
////    // 채널명으로 UUID 호출
////    public UUID findIdByName(String name) {
////        load();
////        for (Map.Entry<UUID, Channel> channel : channels.entrySet()) {
////            if (channel.getValue().getName().equals(name)) {
////                return channel.getKey();
////            }
////        }
////        return null;
////    }
//
//    private final ChannelRepository channelRepository;
//
//    public FileChannelService(ChannelRepository channelRepository) {
//        this.channelRepository = channelRepository;
//    }
//
//    // Create
//    @Override
//    public Channel create(Channel.ChannelType channelType, String name, String description) {
//        // 저장 로직 분리 전
////        channels.put(channel.getId(), channel);
////        save();
////        System.out.println("채널을 생성하였습니다.");
////        System.out.println();
//
//        // 저장 로직 분리 후
//        Channel channel = Channel.create(channelType, name, description);
//        System.out.println("채널을 생성하였습니다.");
//        System.out.println();
//
//        return channel;
//    }
//
//    // Read
//    @Override
//    public Channel readAll(UUID id) {
//        // 저장 로직 분리 전
////        load();
////
////        // NPE 방지
////        if (!channels.containsKey(id)) { System.out.println("해당 채널은 존재하지 않습니다."); }
////        else {
////            Channel channel = channels.get(id);
////            System.out.println("=====채널 정보=====\n" + channel);
////        }
////        System.out.println();
//
//        // 저장 로직 분리 후
//        Channel channel = channelRepository.findById(id);
//        System.out.println("=====채널 정보=====\n" + channel);
//        System.out.println();
//
//        return channel;
//    }
//
//    // Update
//    @Override
//    public Channel updateName(UUID id, String newName) {
//        // 저장 로직 분리 전
////        load();
////
////        // NPE 방지
////        if (!channels.containsKey(id)) { System.out.println("해당 채널은 존재하지 않습니다."); }
////        else {
////            Channel channel = channels.get(id);
////            System.out.println("수정 전 채널 이름 : " + channel.getName());
////            channel.updateName(newName);
////            System.out.println("수정 후 채널 이름 : " + channel.getName());
////            save();
////        }
////        System.out.println();
//
//        // 저장 로직 분리 후
//        Channel channel = channelRepository.findById(id);
//        System.out.println("수정 전 채널 이름 : " + channel.getName());
//        channel.updateName(newName);
//        System.out.println("수정 후 채널 이름 : " + channel.getName());
//        channelRepository.update(channel);
//        System.out.println();
//
//        return channel;
//    }
//
//    @Override
//    public Channel updateGroup(UUID id, String newGroup) {
//        // 저장 로직 분리 전
////        load();
////
////        // NPE 방지
////        if (!channels.containsKey(id)) { System.out.println("해당 채널은 존재하지 않습니다."); }
////        else {
////            Channel channel = channels.get(id);
////            System.out.println("수정 전 속한 채널 그룹 : " + channel.getGroup());
////            channel.updateGroup(newGroup);
////            System.out.println("수정 후 속한 채널 그룹 : " + channel.getGroup());
////            save();
////        }
////        System.out.println();
//
//        // 저장 로직 분리 후
//        Channel channel = channelRepository.findById(id);
//        System.out.println("수정 전 속한 채널 그룹 : " + channel.getGroup());
//        channel.updateGroup(newGroup);
//        System.out.println("수정 후 속한 채널 그룹 : " + channel.getGroup());
//        channelRepository.update(channel);
//        System.out.println();
//
//        return channel;
//    }
//
//    @Override
//    public Channel updateMembersAdd(UUID id, String addMember) {
//        // 저장 로직 분리 전
////        load();
////
////        // NPE 방지
////        if (!channels.containsKey(id)) { System.out.println("해당 채널은 존재하지 않습니다."); }
////        else {
////            Channel channel = channels.get(id);
////            List<String> members = new ArrayList<>(channel.getMembers());
////            System.out.println("수정 전 채널 멤버 : " + channel.getMembers());
////            members.add(addMember);
////            channel.updateMember(members);
////            System.out.println("수정 후 채널 멤버 : " + channel.getMembers());
////            save();
////        }
////        System.out.println();
//
//        // 저장 로직 분리 후
//        Channel channel = channelRepository.findById(id);
//        List<String> members = new ArrayList<>(channel.getMembers());
//        System.out.println("수정 전 채널 멤버 : " + channel.getMembers());
//        members.add(addMember);
//        channel.updateMember(members);
//        System.out.println("수정 후 채널 멤버 : " + channel.getMembers());
//        channelRepository.update(channel);
//        System.out.println();
//
//        return channel;
//    }
//
//    @Override
//    public Channel updateMembersRemove(UUID id, String removeMember) {
//        // 저장 로직 분리 전
////        load();
////
////        // NPE 방지
////        if (!channels.containsKey(id)) { System.out.println("해당 채널은 존재하지 않습니다."); }
////        else {
////            Channel channel = channels.get(id);
////            List<String> members = new ArrayList<>(channel.getMembers());
////            System.out.println("수정 전 채널 멤버 : " + channel.getMembers());
////            members.remove(removeMember);
////            channel.updateMember(members);
////            System.out.println("수정 후 채널 멤버 : " + channel.getMembers());
////            save();
////        }
////        System.out.println();
//
//        // 저장 로직 분리 후
//        // 채널이 존재하는지 확인하는 예외처리
//        Channel channel = channelRepository.findById(id);
//        List<String> members = new ArrayList<>(channel.getMembers());
//        System.out.println("수정 전 채널 멤버 : " + channel.getMembers());
//        members.remove(removeMember);
//        channel.updateMember(members);
//        System.out.println("수정 후 채널 멤버 : " + channel.getMembers());
//        channelRepository.update(channel);
//        System.out.println();
//
//        return channel;
//    }
//
//    // Delete
//    @Override
//    public void delete(UUID id) {
//        // 저장 로직 분리 전
////        load();
////
////        // NPE 방지
////        if (!channels.containsKey(id)) { System.out.println("해당 채널은 존재하지 않습니다."); }
////        else {
////            Channel channel = channels.get(id);
////            System.out.println("채널" + channel.getName() + "이(가) 삭제되었습니다.");
////            channels.remove(id);
////            save();
////        }
////        System.out.println();
//
//        // 저장 로직 분리 후
//        Channel channel = channelRepository.findById(id);
//        System.out.println("채널" + channel.getName() + "이(가) 삭제되었습니다.");
//        channelRepository.delete(id);
//        System.out.println();
//    }
//}
