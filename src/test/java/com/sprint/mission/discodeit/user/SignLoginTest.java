//package com.sprint.mission.discodeit.user;
//
//import com.sprint.mission.discodeit.dto.user.SignUpRequestDTO;
//import com.sprint.mission.discodeit.entity.User;
//import com.sprint.mission.discodeit.repository.UserRepository;
//import com.sprint.mission.discodeit.service.UserService;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//@SpringBootTest
//public class SignLoginTest {
//
//    @Autowired
//    private UserService userService;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Test
//    @DisplayName("회원가입 테스트")
//    void signUpTest() throws InterruptedException {
//        SignUpRequestDTO signUpRequestDTO = SignUpRequestDTO.builder()
//                                        .username("test")
//                                        .email("test@gmail.com")
//                                        .password("test")
//                                        .build();
//
//        User user = userService.signUp(signUpRequestDTO);
//        User signUser = userRepository.findByEmail(user.getEmail())
//                        .orElseThrow(() -> new RuntimeException("error"));
////
////        Assertions.assertThat(user).isEqualTo(signUser);
//    }
////}
