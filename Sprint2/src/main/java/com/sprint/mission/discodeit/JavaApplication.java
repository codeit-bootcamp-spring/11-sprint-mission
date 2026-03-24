package com.sprint.mission.discodeit;

import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.UserService;
import com.sprint.mission.discodeit.service.jcf.JCFUserService;

public class JavaApplication {
    public static void main(String[] args) {
        UserService userService = new JCFUserService();

        User user = new User("Choi");
        userService.create(user);
        System.out.println("Created: " + userService.findById(user.getId()).getName());

        userService.update(user.getId(), "Choi Woo-jun");
        System.out.println("Updated: " + userService.findById(user.getId()).getName());

        userService.delete(user.getId());
        System.out.println("Deleted (is null?): " + (userService.findById(user.getId()) == null));
    }
}